# AGENTS.md

## Project objective

Build and maintain a production-minded, single-service Spring Boot implementation of a multi-tenant notification platform. Preserve strict tenant isolation, deterministic tests, persisted delivery history, and bounded concurrency.

## Architecture

Organize code by feature, then by responsibility inside each feature:

- `auth`: login, JWT parsing, principals, security rules, bootstrap administrator.
- `tenant`: tenants, users' tenant ownership, channel configuration, and rate policies.
- `template`: mutable MVP templates and variable rendering. Rendered delivery content is immutable.
- `delivery`: notification batches, delivery state machine, provider attempts, dispatcher, retries, and reports.
- `shared`: API errors, pagination, time, and cross-cutting HTTP concerns.

Each feature uses `api`, `application`, `domain`, and `infrastructure` packages. Dependencies flow from API to application to domain; infrastructure implements persistence and integration concerns.

Feature packages may depend on `shared`. Delivery may depend on tenant and template domain contracts. Template must not depend on delivery. Controllers must delegate business rules to services; repositories must not be called directly from unrelated feature controllers.

## Engineering rules

- Use Java 21 and Spring Boot 3. Keep configuration in `.properties` files.
- Use constructor injection. Do not add field injection.
- Use ordinary, explicitly named Java classes for API DTOs and principals. Do not use Java records or generic map responses.
- Keep one public type per source file and run Spotless before handoff.
- Keep database changes in versioned Flyway migrations; never use Hibernate schema generation outside tests.
- Every tenant-owned lookup must include or verify the authenticated tenant ID. Return `404` for cross-tenant resource probes.
- Keep state transitions inside transactional services and append a `DeliveryEvent` for every transition.
- Treat `Idempotency-Key` as tenant-scoped. Never infer deduplication from recipient or payload content.
- Use UTC `Instant` values at API and persistence boundaries.
- Keep provider behavior behind an adapter. A real provider must receive the stable delivery provider key.
- Never log JWTs, passwords, rendered message bodies, or provider credentials.

## Tests and verification

- Run `./gradlew test` before handing off changes.
- Add focused unit tests for pure business rules and H2 integration tests for persistence, security, and API flows.
- Tests must be deterministic: use configured provider outcomes, not random failures or timing-sensitive sleeps.
- Cover tenant isolation, invalid transitions, idempotency, template validation, rate limiting, retries, and audit history when those areas change.

## Submission workflow

- Keep `README.md`, this file, migrations, test fixtures, and `docs/skills-used.md` in the repository.
- Make small, descriptive commits during development. Do not commit secrets, generated build output, IDE state, or local database files.
- In the demo, explain the feature boundaries, security model, delivery state machine, rate-limiting/fairness approach, retry/idempotency guarantees, and test strategy.

## AI development record

Codex was used to inspect the starter, plan the domain model, implement the service, and create tests/documentation. The reusable local `spring-notification-architecture` skill was created for future architecture work; the exact record is in `docs/skills-used.md`.
