# Testing Strategy

Run all checks with:

```bash
./gradlew spotlessCheck test
```

- Unit tests cover template rendering and delivery transition rules.
- H2 integration tests run Flyway, start the complete Spring context, authenticate both roles, and execute the core tenant/template/submission flow.
- The built-in local provider has deterministic demo recipients: normal addresses succeed,
  `transient-once@provider.test` fails once and then succeeds,
  `transient-always@provider.test` fails until attempts are exhausted, and
  `permanent-failure@provider.test` fails without retrying. A real provider adapter replaces
  this resolver.
- Tests must not rely on random failures or timing sleeps.

The next highest-value cases are tenant-isolation probes, idempotency conflicts, transient retry/exhaustion, rate-limit deferral, and fair dispatcher ordering.

## Why this test split

| Layer | What it tests | Why it belongs there |
| --- | --- | --- |
| Unit | Rendering, transition validity, retry delay, request hash, rate limiter, round-robin selection, and provider outcomes. | These rules are narrow and failures are fast to diagnose. |
| Integration | Flyway, JPA mapping, Spring Security, JWT authentication, validation, errors, and tenant/template/submission flow. | Unit tests would miss configuration and persistence mistakes at these boundaries. |
| Manual API proof | The Swagger flow from platform admin to delivery report. | Confirms the public API is usable in the same sequence as a real client. |

## Determinism is a design requirement

The local provider maps specific recipients to success, one transient failure, repeated transient failure, or permanent failure. Retry behavior can therefore be asserted without network access, randomness, or sleep-based timing. UTC time and configured policy values keep scheduling and backoff reproducible.

## Coverage priorities

The current suite covers primary business rules and the core end-to-end flow. The next additions are explicit cross-tenant 404 probes, idempotency conflict API coverage, retry exhaustion persistence, rate-limit deferral, and fair ordering with multiple active tenants.
