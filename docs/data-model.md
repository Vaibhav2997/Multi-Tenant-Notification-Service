# Data Model

~~~mermaid
erDiagram
    TENANTS ||--o{ APP_USERS : owns
    TENANTS ||--o{ CHANNEL_CONFIGURATIONS : configures
    TENANTS ||--o{ TEMPLATES : owns
    TENANTS ||--o{ NOTIFICATION_BATCHES : submits
    NOTIFICATION_BATCHES ||--o{ DELIVERIES : contains
    DELIVERIES ||--o{ DELIVERY_ATTEMPTS : records
    DELIVERIES ||--o{ DELIVERY_EVENTS : audits
~~~

```text
tenants ─┬─< app_users
         ├─< tenant_rate_limits
         ├─< channel_configurations
         ├─< templates
         └─< notification_batches ─< deliveries ─┬─< delivery_attempts
                                                 └─< delivery_events

platform_policy                     provider_receipts
```

| Table | Purpose and design reason |
|---|---|
| `tenants` | Root ownership boundary used to isolate all tenant data. |
| `app_users` | Stores BCrypt credentials and either platform or tenant-admin role. `tenant_id` is null only for platform admins. |
| `platform_policy` | Singleton row containing global rate, batch-size, and retry defaults managed by the platform admin. |
| `tenant_rate_limits` | Optional tenant override; absence means the global rate applies. |
| `channel_configurations` | One row per tenant/channel, keeping provider enablement and sender identity separate from content. |
| `templates` | Mutable tenant/channel content. Deliveries store rendered snapshots so later edits cannot alter queued messages. |
| `notification_batches` | One client submission. The tenant/key unique constraint and request hash implement safe API idempotency. |
| `deliveries` | One row per recipient, with independently retryable status, due time, rendered content, and stable provider key. |
| `delivery_attempts` | One row per actual provider call, including outcome and safe diagnostic details. Rate-limit deferrals are not attempts. |
| `delivery_events` | Immutable delivery state-transition audit trail. |
| `provider_receipts` | Durable deduplication ledger used by the local provider adapter. |

## Entity decisions

### Why batches and deliveries are separate

A batch is the client request and idempotency boundary; a delivery is one recipient's independently scheduled and retryable unit of work. One recipient can fail or be deferred without changing the history of other recipients. Batch status is therefore an aggregate view, not the worker's unit of processing.

### Why templates are mutable but deliveries are immutable

Templates are tenant-managed working content. A delivery persists rendered subject and body at acceptance time. This makes audit reports truthful and prevents a later template edit from silently changing scheduled work.

### Why deliveries repeat tenant ownership

Deliveries store tenant ownership even though their batch has a tenant. This makes tenant-scoped report queries direct and supports an explicit tenant filter at the most sensitive query boundary. The application verifies ownership on every tenant-facing lookup.

### Why policy is global plus optional override

The platform policy provides one managed default for batch, retry, and rate behavior. A tenant override exists only for exceptions, avoiding copied defaults for every tenant and making the effective policy easy to reason about.

## Delivery states

```text
SCHEDULED → QUEUED → PROCESSING → DELIVERED
                         │
                         ├─ transient → RETRY_SCHEDULED → QUEUED
                         └─ permanent/exhausted → FAILED
```

`DELIVERED` and `FAILED` are terminal. Domain code rejects invalid transitions.

## Important indexes and constraints

- Unique tenant slug and user email.
- Unique `(tenant_id, channel)` channel configuration.
- Unique `(tenant_id, name, channel)` template.
- Unique `(tenant_id, idempotency_key)` notification batch.
- Unique provider idempotency key.
- Due-work index on `(status, next_attempt_at)`.
- Tenant report indexes on tenant, status, and creation time.

The relational constraints enforce ownership, uniqueness, and safe retry semantics in addition to application code. SQL is intentional: delivery reporting and transactional state changes depend on these relationships.
