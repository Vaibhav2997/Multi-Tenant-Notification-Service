# Multi-tenant Notification Service

A submission-focused Spring Boot 3 / Java 21 modular monolith supporting tenant-scoped templates, four notification channels, immediate and UTC-scheduled batches, bounded fair dispatch, rate limiting, retries, provider idempotency, and delivery audit reports.

## Implemented MVP

- JWT authentication with `PLATFORM_ADMIN` and `TENANT_ADMIN` authorization.
- Tenant provisioning, global delivery policy, and tenant rate overrides.
- EMAIL, SMS, PUSH, and IN_APP configuration.
- Tenant templates using `${variable}` substitution.
- Multi-recipient immediate or scheduled notification batches.
- Tenant-scoped `Idempotency-Key` with conflicting-payload detection.
- Bounded worker pool, rotating per-tenant round-robin dispatch, and per-tenant token buckets.
- Exponential retry of transient failures and terminal permanent failures.
- Durable local-provider idempotency receipts.
- Deterministic local-provider recipients for demonstrating retry and permanent-failure paths.
- Persisted delivery attempts and state-transition events.
- Tenant-scoped, paginated delivery reports.
- OpenAPI and Swagger UI.

The exact scope and deferred features are documented in [MVP requirements](docs/requirements.md).

## Architecture and documentation

- [Architecture](docs/architecture.md)
- [Data model and table rationale](docs/data-model.md)
- [API guide](docs/api-guide.md)
- [Testing strategy](docs/testing.md)
- [Video demo script](docs/demo-script.md)
- [AI/skills record](docs/skills-used.md)
- [Development instructions](AGENTS.md)

Code is organized by feature (`auth`, `tenant`, `template`, `delivery`), then by `api`, `application`, `domain`, and `infrastructure` inside each feature.

## Prerequisites

- Java 21
- PostgreSQL 18 running on port 5432

This machine's EnterpriseDB installation provides binaries under `/Library/PostgreSQL/18/bin`:

```bash
export PATH="/Library/PostgreSQL/18/bin:$PATH"
```

Create an application role and database using the PostgreSQL password selected during installation:

```bash
psql -h localhost -U postgres -d postgres -W
```

```sql
CREATE ROLE notification_app WITH LOGIN PASSWORD 'choose-a-password';
CREATE DATABASE notifications OWNER notification_app;
```

## Run

```bash
export DATABASE_URL='jdbc:postgresql://localhost:5432/notifications'
export DATABASE_USERNAME='notification_app'
export DATABASE_PASSWORD='choose-a-password'
export NOTIFICATION_JWT_SECRET="$(openssl rand -base64 32)"

./gradlew bootRun
```

Flyway automatically creates and validates the schema. The bootstrap account defaults to:

```text
email: platform@example.com
password: change-me-now
```

Override it with `BOOTSTRAP_ADMIN_EMAIL` and `BOOTSTRAP_ADMIN_PASSWORD` before any shared environment use.
On later startups, the existing account must still be active, tenant-free, assigned `PLATFORM_ADMIN`, and match the configured password; otherwise startup fails instead of silently accepting inconsistent bootstrap credentials.

## API documentation

After startup:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- OpenAPI YAML: `http://localhost:8080/v3/api-docs.yaml`

All protected APIs use `Authorization: Bearer <token>`.

## Tests

The test profile uses an in-memory H2 database in PostgreSQL compatibility mode.

```bash
./gradlew spotlessCheck test
```

## Important delivery semantics

- Scheduled content is rendered and snapshotted at submission time.
- Recipient variables override batch variables.
- Transient failures retry with capped exponential backoff; permanent failures do not retry.
- Worker execution is at-least-once. Providers receive a stable idempotency key; the included local provider persists accepted keys to prevent repeat acceptance.
- The MVP is intentionally single-instance. Distributed scheduling and locking are deferred.
