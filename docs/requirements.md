# Product Scope and Requirements

## Product statement

This service is a single-deployment, multi-tenant notification platform. A platform administrator
creates and governs tenants. A tenant administrator configures that tenant's channels and
templates, submits immediate or scheduled notification batches, and views only that tenant's
delivery history.

The product is designed around four non-negotiable outcomes:

1. Tenant data must not be exposed across tenant boundaries.
2. An accepted notification must have durable, auditable delivery history.
3. Retries and client resubmissions must not create an unintended duplicate delivery.
4. One tenant's workload must not monopolize bounded worker capacity.

## Functional requirements

| ID | Requirement | Public surface | Design evidence |
| --- | --- | --- | --- |
| FR-01 | Authenticate platform and tenant administrators and enforce their distinct roles. | POST /api/v1/auth/login | JWT role claims, security rules, app_users |
| FR-02 | Allow a platform administrator to create tenants and tenant administrators. | /api/v1/platform/tenants | tenants and app_users |
| FR-03 | Allow a platform administrator to manage global delivery policy and tenant rate overrides. | /api/v1/platform/policy and /api/v1/platform/tenants/{tenantId}/rate-limit | platform_policy and tenant_rate_limits |
| FR-04 | Allow a tenant administrator to enable or configure each notification channel. | /api/v1/tenant/channels | channel_configurations |
| FR-05 | Allow a tenant administrator to create, edit, activate, and query templates with variables. | /api/v1/tenant/templates | templates and template renderer |
| FR-06 | Accept immediate or UTC-scheduled, multi-recipient notification batches. | POST /api/v1/tenant/notification-batches | notification_batches and deliveries |
| FR-07 | Make client submission idempotent within a tenant. | Idempotency-Key header | tenant-scoped key and canonical request hash |
| FR-08 | Dispatch due delivery work with bounded concurrency and fair tenant selection. | Internal scheduler | bounded executor and rotating tenant selection |
| FR-09 | Enforce a per-tenant token-bucket rate limit. | Internal dispatcher | effective platform/tenant policy and limiter |
| FR-10 | Retry transient provider failures with capped exponential backoff; fail permanent errors immediately. | Internal worker | next attempt time, attempts, and state machine |
| FR-11 | Pass a stable delivery idempotency key to the provider. | Provider adapter contract | provider_receipts in local adapter |
| FR-12 | Expose tenant-scoped batch status, delivery reports, attempts, and state-transition history. | /api/v1/tenant/notification-batches and /api/v1/tenant/deliveries | deliveries, delivery_attempts, delivery_events |
| FR-13 | Return not found for a cross-tenant resource probe. | All tenant endpoints | authenticated tenant checks and scoped queries |
| FR-14 | Validate public input and return consistent RFC Problem Details errors. | All HTTP endpoints | DTO validation and shared error handling |

## Roles and ownership

| Role | Allowed responsibilities | Explicitly excluded |
| --- | --- | --- |
| PLATFORM_ADMIN | Tenant provisioning, tenant-admin provisioning, global policy, and per-tenant rate overrides. | Editing a tenant's templates or viewing delivery reports through tenant APIs. |
| TENANT_ADMIN | Its own channel configuration, templates, batch submission, and delivery reports. | Managing another tenant, global policy, or another tenant's data. |

Tenant identity is derived from the authenticated principal. Tenant API paths intentionally do not
make a caller-supplied tenant ID the source of authority.

## Delivery and consistency rules

- A template is mutable, but every accepted delivery stores an immutable rendered snapshot.
- A batch is the idempotency boundary; a delivery is the independently retryable unit for one recipient.
- The same tenant key and canonical request return the existing batch. The same key with different content returns 409 Conflict.
- Every provider invocation creates an attempt record; every status transition creates an event record.
- Worker execution is at least once. Provider-level duplicate protection depends on the provider honoring the stable delivery key.
- All API and persistence timestamps use UTC Instant values.

## Scope boundary

This implementation deliberately provides a strong single-instance foundation. The following are
not part of the current product scope:

- Recurring schedules, cancellation, template versioning, exports, or a frontend.
- Real third-party provider credentials or production provider adapters.
- Multi-instance delivery claims, distributed locking, or distributed rate limiting.
- OAuth, SSO, MFA, deployment automation, production observability, and alerting.

These exclusions prevent claims the implementation does not make. The existing module boundaries,
provider adapter, Flyway schema, and delivery state model are intended to support future additions
without changing the established tenant and audit invariants.

## Verification approach

The project uses focused unit tests for rendering, transitions, hashing, backoff, rate limiting,
fair selection, and deterministic provider outcomes. H2 integration tests run Flyway and the full
Spring security/API flow. The complete repository check is:

~~~bash
./gradlew spotlessCheck test
~~~

The detailed rationale is in architecture.md, data-model.md, api-guide.md, and testing.md; each is
written to stand on its own without requiring external context.
