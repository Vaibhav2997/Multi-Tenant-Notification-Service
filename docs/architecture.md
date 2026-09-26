# Architecture

## Solution at a glance

This is a multi-tenant notification platform for platform administrators and tenant administrators. It accepts tenant-defined templates and immediate or UTC-scheduled recipient batches, then delivers work through a bounded, fair dispatcher while retaining a durable audit history.

The design is driven by four priorities:

1. Tenant isolation: one tenant must never read, configure, or deliver against another tenant's data.
2. Delivery correctness: an accepted message has immutable rendered content, controlled state transitions, and a durable history.
3. Safe retries: both clients and workers can repeat work without creating an unintended duplicate.
4. Fair bounded execution: a busy tenant cannot consume unbounded memory or monopolize available workers.

The solution is intentionally a production-minded single-instance modular monolith. It provides strong transactional guarantees within one deployment and makes multi-instance limitations explicit rather than implying distributed guarantees it does not implement.

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

### Component responsibilities

| Component | Responsibility | Boundary it protects |
| --- | --- | --- |
| Auth | Password verification, JWT creation/parsing, and current principal. | A request gets one authenticated role and, for tenant users, one tenant identity. |
| Tenant | Tenant lifecycle, channel configuration, global policy, and tenant rate override. | Platform-wide actions remain separate from tenant operations. |
| Template | Tenant-owned, mutable source templates and variable validation/rendering. | Later template edits cannot alter delivery snapshots. |
| Delivery | Idempotent submission, batches, deliveries, attempts, events, reports, retries, and state rules. | The delivery lifecycle is centralized instead of scattered across controllers or providers. |
| Dispatcher | Due-work selection, tenant rotation, bounded task submission, and rate-limit coordination. | One tenant cannot consume all worker capacity. |
| Provider adapter | Translation from a stable delivery command to a provider call. | Provider behavior and duplicate protection do not leak into domain logic. |
| PostgreSQL | Tenant ownership, policy, submissions, delivery state, and audit records. | Relationships and uniqueness rules are enforced durably, not only in memory. |

### Why a modular monolith

The assignment needs transactional delivery handling, role-based access, scheduled work, and durable audit history, but does not require distributed systems. A single Spring Boot deployment avoids network calls and distributed transactions between auth, templates, and delivery. Feature packages still prevent an unstructured monolith and leave clear seams for future provider or scaling changes.

API code validates HTTP input and delegates; application services own use cases and transactions; domain code holds entities and state rules; infrastructure implements persistence, JWT parsing, scheduling, and provider behavior. Template does not depend on delivery, so delivery lifecycle complexity cannot leak into template editing.

## Tenant isolation and security model

There are two roles. PLATFORM_ADMIN manages tenants, tenant administrators, global policy, and tenant-specific rate overrides. TENANT_ADMIN manages only its own channel configuration and templates, submits batches, and reads its own delivery reports.

~~~mermaid
flowchart LR
    Platform[Platform admin JWT] --> PlatformApi[Platform administration API]
    Tenant[Tenant admin JWT] --> TenantApi[Tenant API]
    PlatformApi --> Policy[Tenants and delivery policy]
    TenantApi --> PrincipalTenant[Authenticated tenant identity]
    PrincipalTenant --> ScopedServices[Tenant-scoped application services]
    ScopedServices --> TenantRows[(Rows for that tenant only)]
~~~

Tenant endpoints derive ownership from the authenticated principal; a caller does not supply a tenant ID to establish access. Every tenant-owned lookup filters or verifies that identity, and a cross-tenant resource probe is returned as not found. This avoids both unauthorized data access and unnecessary disclosure that another tenant's resource exists.

JWT is used because the system has only two administrator roles and no requirement for OAuth, SSO, or MFA. The signing secret is runtime-injected, never part of the application source, and the service does not log tokens, passwords, rendered bodies, or provider credentials.

## Why relational persistence

PostgreSQL is a deliberate architectural choice rather than a generic database default. The workflow has strong relationships and consistency constraints: tenants own users, templates, channels, batches, and deliveries; deliveries own attempts and events; and a tenant plus idempotency key must be unique. Submission and completion update related records atomically.

Foreign keys, unique constraints, indexes, joins, and ACID transactions make those rules declarative and make tenant-scoped reporting direct. A document store could represent the same objects, but would move important integrity rules into application code and make delivery and audit queries less natural. Flyway versions the schema so the persistence contract is reviewable and repeatable across environments.

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

## Delivery state machine

The delivery state is the lifecycle authority. The entity rejects every transition not listed
below, so a controller, dispatcher, or provider cannot skip directly to a terminal state. A state
change is performed by the transactional delivery-state service and persists one audit event with
the previous state, new state, reason, and UTC timestamp.

~~~mermaid
stateDiagram-v2
    [*] --> SCHEDULED: future scheduled time
    [*] --> QUEUED: immediate submission
    SCHEDULED --> QUEUED: scheduled time reached
    RETRY_SCHEDULED --> QUEUED: retry delay elapsed
    QUEUED --> PROCESSING: worker claims delivery
    PROCESSING --> DELIVERED: provider accepts
    PROCESSING --> RETRY_SCHEDULED: transient failure and attempts remain
    PROCESSING --> FAILED: permanent failure or retry budget exhausted
    DELIVERED --> [*]
    FAILED --> [*]
~~~

| From | To | Trigger and persisted work |
| --- | --- | --- |
| Initial | SCHEDULED | Submission has a future UTC scheduled time. The rendered snapshot and next due time are stored with the delivery. |
| Initial | QUEUED | Submission is immediate. The delivery is eligible for dispatch at its next due time. |
| SCHEDULED | QUEUED | The dispatcher finds due work and the worker claims it. An event records that scheduled time was reached. |
| RETRY_SCHEDULED | QUEUED | The retry due time has elapsed and the worker claims it. An event records that the retry delay elapsed. |
| QUEUED | PROCESSING | A worker obtains the delivery lock, claims it, increments the attempt count, creates an open DeliveryAttempt, and records a claim event. |
| PROCESSING | DELIVERED | The provider accepts the notification. The attempt becomes DELIVERED, the delivery becomes terminal, and an event records acceptance. |
| PROCESSING | RETRY_SCHEDULED | The provider reports a transient failure and attempts remain. The attempt stores safe failure diagnostics, exponential backoff sets next due time, and an event records the retry decision. |
| PROCESSING | FAILED | The provider reports a permanent failure, or the transient failure uses the final allowed attempt. The attempt is completed, the delivery becomes terminal, and an event records the reason. |

### Claim, provider call, and completion

The claim is deliberately short and locked: it verifies that the delivery is due, moves any
scheduled or retry-scheduled item through QUEUED, then moves it to PROCESSING. It also creates the
attempt record before returning the provider command. The provider call happens after the claim
transaction completes. Completion locks the same delivery again, writes the provider result to the
attempt, applies exactly one valid terminal or retry transition, and recalculates aggregate batch
status when every delivery is terminal.

### Rate-limit deferral is not an attempt

The token bucket is checked before the worker claims a delivery. If the tenant has no token, the
delivery remains in its current eligible state and its next due time moves forward one second.
There is no provider call, attempt record, or state-transition event because no delivery attempt
occurred. This distinction keeps provider-attempt counts and audit history meaningful.

### Terminal and invalid states

DELIVERED and FAILED are terminal; neither can move to another state. Any unlisted transition
raises an error in the domain entity. A duplicate or late worker completion is ignored unless the
delivery is still PROCESSING, which protects the lifecycle from a repeated completion call.

## Concurrency and fairness

The worker count and queue capacity are fixed configuration values. The dispatcher never deliberately creates unbounded tasks. Due work is first partitioned by tenant, and the starting tenant rotates between polls. `notification.dispatch.tenant-burst` limits the number selected for one tenant during a polling cycle.

The MVP targets one application instance. A future multi-instance design would add database leases or `SKIP LOCKED` claiming.

### How fair dispatch works

1. On each poll, the dispatcher finds tenants with due eligible deliveries.
2. It rotates the starting tenant from the previous poll, so the same tenant does not always win first position.
3. It reads at most the configured burst for each tenant.
4. A round-robin selector takes one delivery from each tenant queue at a time until executor capacity is filled.
5. The task enters a fixed-size worker pool and bounded queue; duplicate in-process submission is suppressed.
6. Before a worker claims provider work, the tenant token bucket must grant a token.

This is fairness at selection time and rate control at processing time. It does not promise global distributed ordering, which would need a different coordination model.

## Delivery guarantees

- Submission deduplication: tenant-scoped `Idempotency-Key` plus request hash.
- Worker behavior: at-least-once.
- Provider deduplication: stable provider key; the local provider persists accepted keys in `provider_receipts`.
- Auditability: every provider call has an attempt row and every state change has an event row.

### What the system guarantees, and what it does not

| Concern | Current guarantee | Intentional non-guarantee |
| --- | --- | --- |
| Client retries | Tenant-scoped key plus canonical request hash returns the original batch or a conflict. | It does not deduplicate unrelated submissions with similar recipients or content. |
| Provider retries | The provider receives a stable delivery key on each attempt. | Exactly-once delivery across an external provider is not claimed. |
| Template history | Each accepted delivery has a rendered snapshot. | Later template edits do not retrofit already accepted messages. |
| Audit | Provider attempts and valid status transitions are persisted. | This is not a full observability or analytics platform. |
| Concurrency | Worker count and queue are bounded, with rotating tenant selection. | Multi-instance claiming, locking, and global fairness are not implemented. |

### Explicit trade-offs

| Decision | Benefit | Deliberate limitation |
| --- | --- | --- |
| Render at submission | Scheduled work remains historically accurate after template edits. | Submitted work does not inherit later template fixes. |
| Tenant key plus request hash | Safe client retry and conflicting reuse returns 409. | Clients must preserve a logical submission key. |
| At-least-once worker plus provider key | Crash recovery without duplicate provider acceptance when the provider honors the key. | Exactly-once delivery cannot be guaranteed across an external boundary. |
| In-memory token bucket | Simple, deterministic limiting in one process. | State is not shared between instances. |
| Local provider adapter | Deterministic tests without external credentials. | It is a contract demonstration, not a production integration. |
