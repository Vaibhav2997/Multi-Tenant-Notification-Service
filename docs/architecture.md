# Architecture

## Modular monolith

The service is a single deployable Spring Boot application organized by feature. Each feature contains `api`, `application`, `domain`, and `infrastructure` packages.

~~~mermaid
flowchart TB
    Client[Platform or tenant administrator] --> Api[REST API]
    Api --> Auth[auth]
    Api --> Tenant[tenant]
    Api --> Template[template]
    Api --> Delivery[delivery]
    Auth --> Db[(PostgreSQL)]
    Tenant --> Db
    Template --> Db
    Delivery --> Db
    Delivery --> Dispatch[bounded dispatcher]
    Dispatch --> Provider[provider adapter]
    Provider --> Db
~~~

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

### Why a modular monolith

The assignment needs transactional delivery handling, role-based access, scheduled work, and durable audit history, but does not require distributed systems. A single Spring Boot deployment avoids network calls and distributed transactions between auth, templates, and delivery. Feature packages still prevent an unstructured monolith and leave clear seams for future provider or scaling changes.

API code validates HTTP input and delegates; application services own use cases and transactions; domain code holds entities and state rules; infrastructure implements persistence, JWT parsing, scheduling, and provider behavior. Template does not depend on delivery, so delivery lifecycle complexity cannot leak into template editing.

## Submission flow

1. Validate role, tenant, batch size, schedule, active template, enabled channel, and variables.
2. Calculate a canonical SHA-256 request hash.
3. Return the existing batch for the same tenant/key/hash; reject a different hash with `409`.
4. Persist one batch and one rendered delivery per recipient in a single transaction.
5. The dispatcher selects due tenants and submits a small burst per tenant in rotating round-robin order.
6. A worker checks the tenant token bucket, transactionally claims the delivery, then calls the provider outside that transaction.
7. A second transaction persists the provider result, retry time, delivery event, and aggregate batch status.

Batch creation and rendered snapshots are persisted in one transaction, so acceptance is all-or-nothing. A worker claims work in a short transaction, calls the provider outside the transaction, then persists the result in a second transaction. This avoids holding database locks across an external provider call.

~~~mermaid
sequenceDiagram
    participant Admin as Tenant admin
    participant Submission as Submission service
    participant DB as PostgreSQL
    participant Worker as Worker
    participant Provider as Provider adapter
    Admin->>Submission: Batch and idempotency key
    Submission->>DB: Verify key and request hash
    Submission->>DB: Persist batch and rendered deliveries
    Worker->>DB: Claim delivery and append event
    Worker->>Provider: Send stable provider key
    Provider-->>Worker: Provider result
    Worker->>DB: Persist attempt, transition, and event
~~~

## Concurrency and fairness

The worker count and queue capacity are fixed configuration values. The dispatcher never deliberately creates unbounded tasks. Due work is first partitioned by tenant, and the starting tenant rotates between polls. `notification.dispatch.tenant-burst` limits the number selected for one tenant during a polling cycle.

The MVP targets one application instance. A future multi-instance design would add database leases or `SKIP LOCKED` claiming.

## Delivery guarantees

- Submission deduplication: tenant-scoped `Idempotency-Key` plus request hash.
- Worker behavior: at-least-once.
- Provider deduplication: stable provider key; the local provider persists accepted keys in `provider_receipts`.
- Auditability: every provider call has an attempt row and every state change has an event row.

### Explicit trade-offs

| Decision | Benefit | Deliberate limitation |
| --- | --- | --- |
| Render at submission | Scheduled work remains historically accurate after template edits. | Submitted work does not inherit later template fixes. |
| Tenant key plus request hash | Safe client retry and conflicting reuse returns 409. | Clients must preserve a logical submission key. |
| At-least-once worker plus provider key | Crash recovery without duplicate provider acceptance when the provider honors the key. | Exactly-once delivery cannot be guaranteed across an external boundary. |
| In-memory token bucket | Simple, deterministic limiting in one process. | State is not shared between instances. |
| Local provider adapter | Deterministic tests without external credentials. | It is a contract demonstration, not a production integration. |
