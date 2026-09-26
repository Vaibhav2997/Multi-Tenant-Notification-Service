# Architecture

## Modular monolith

The service is a single deployable Spring Boot application organized by feature. Each feature contains `api`, `application`, `domain`, and `infrastructure` packages.

```text
HTTP request
    ↓
feature.api                 Controllers and DTO validation
    ↓
feature.application         Use cases and transaction orchestration
    ↓
feature.domain              Entities, enums and state rules
    ↑
feature.infrastructure     JPA repositories, JWT, scheduling and providers
```

The four features are:

- `auth`: credentials, JWT creation/validation, and the authenticated principal.
- `tenant`: tenants, platform policy, tenant rate overrides, and channel configuration.
- `template`: tenant-scoped template management and variable rendering.
- `delivery`: idempotent batch submission, deliveries, attempts, events, provider dispatch, retries, and reports.

`shared` contains only HTTP error handling, pagination, OpenAPI configuration, and the UTC clock.

## Submission flow

1. Validate role, tenant, batch size, schedule, active template, enabled channel, and variables.
2. Calculate a canonical SHA-256 request hash.
3. Return the existing batch for the same tenant/key/hash; reject a different hash with `409`.
4. Persist one batch and one rendered delivery per recipient in a single transaction.
5. The dispatcher selects due tenants and submits a small burst per tenant in rotating round-robin order.
6. A worker checks the tenant token bucket, transactionally claims the delivery, then calls the provider outside that transaction.
7. A second transaction persists the provider result, retry time, delivery event, and aggregate batch status.

## Concurrency and fairness

The worker count and queue capacity are fixed configuration values. The dispatcher never deliberately creates unbounded tasks. Due work is first partitioned by tenant, and the starting tenant rotates between polls. `notification.dispatch.tenant-burst` limits the number selected for one tenant during a polling cycle.

The MVP targets one application instance. A future multi-instance design would add database leases or `SKIP LOCKED` claiming.

## Delivery guarantees

- Submission deduplication: tenant-scoped `Idempotency-Key` plus request hash.
- Worker behavior: at-least-once.
- Provider deduplication: stable provider key; the local provider persists accepted keys in `provider_receipts`.
- Auditability: every provider call has an attempt row and every state change has an event row.
