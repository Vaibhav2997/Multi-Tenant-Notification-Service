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
