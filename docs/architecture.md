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
