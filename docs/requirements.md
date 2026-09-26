# MVP Requirements

The MVP implements the original assignment without adding recurring schedules, real provider integrations, distributed coordination, cancellation, template versioning, exports, or a UI.

| ID | Requirement | API | Persistence | Verification |
|---|---|---|---|---|
| FR-01 | JWT authentication and platform/tenant roles | `POST /api/v1/auth/login` | `app_users` | Login and role integration tests |
| FR-02 | Platform tenant and tenant-admin management | `/api/v1/platform/tenants/**` | `tenants`, `app_users` | Platform workflow integration test |
| FR-03 | Global and tenant rate limits | `/api/v1/platform/policy`, `/rate-limit` | `platform_policy`, `tenant_rate_limits` | Limiter unit/integration tests |
| FR-04 | Tenant channel configuration | `/api/v1/tenant/channels/**` | `channel_configurations` | Channel flow integration test |
| FR-05 | Tenant templates and variable substitution | `/api/v1/tenant/templates/**` | `templates` | Renderer unit tests and API flow |
| FR-06 | Immediate and UTC-scheduled recipient batches | `/api/v1/tenant/notification-batches` | `notification_batches`, `deliveries` | Submission integration tests |
| FR-07 | Tenant-scoped submission idempotency | `Idempotency-Key` request header | Batch key and request hash | Repeat/conflict tests |
| FR-08 | Bounded concurrent and fair dispatch | Internal scheduler | Delivery due-time indexes | Dispatcher tests |
| FR-09 | Per-tenant token-bucket limiting | Internal dispatcher | Policy tables; bucket is in-memory for MVP | Limiter tests |
| FR-10 | Transient retries with exponential backoff | Internal worker | Delivery due time and attempts | Retry tests |
| FR-11 | Duplicate protection on provider retry | Internal provider contract | `provider_receipts` | Provider idempotency tests |
| FR-12 | Delivery status, attempts, and audit timeline | `/api/v1/tenant/deliveries/**` | `deliveries`, `delivery_attempts`, `delivery_events` | Report integration tests |
| FR-13 | Tenant isolation | All tenant endpoints | Tenant foreign keys and scoped queries | Cross-tenant tests |
| FR-14 | Validation and consistent error responses | All APIs | N/A | Validation integration tests |

## Roles

- `PLATFORM_ADMIN`: manages tenants, tenant administrators, the global delivery policy, and tenant rate overrides.
- `TENANT_ADMIN`: manages its tenant's channels and templates, submits notifications, and views its delivery reports.

## MVP assumptions

- One Spring Boot process is deployed. Distributed coordination is outside scope.
- Scheduling uses one UTC `Instant`; recurring schedules are excluded.
- Templates are mutable, but rendered content is snapshotted on delivery creation.
- The local provider is successful by default and exposes documented deterministic test
  recipients for transient-once, transient-until-exhausted, and permanent failure paths.
- Provider execution is at-least-once. Effective deduplication depends on the provider honoring the stable idempotency key.
