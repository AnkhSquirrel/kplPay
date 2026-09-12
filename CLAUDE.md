# AGENTS.md

## Project Purpose

kplPay — a simulated B2B SaaS subscription billing backend. Portfolio project built to
demonstrate production-grade Java backend engineering: subscription and invoice domain
modeling, real third-party API integration (INSEE Sirene company lookup, Stripe test-mode
payments), financial calculation logic, PDF generation, and a working CI/CD pipeline.
The domain mirrors prior professional experience (invoicing, third-party API integration,
financial indicator calculation) rather than a generic CRUD tutorial. The goal is a codebase
that is interview-defensible — every decision should be explainable, not just functional.

## Tech Stack

- Language/runtime: Java 25
- Framework: Spring Boot 4.1.1
- Build: Maven, via `./mvnw` wrapper only — never rely on a system-installed `mvn`
- Database: PostgreSQL 17 (alpine image), schema owned exclusively by Flyway
- Testing: JUnit 5 + Mockito (unit), Testcontainers (integration, real Postgres),
  WireMock (mocked external HTTP — INSEE, Stripe)
- External integrations: INSEE Sirene API (company lookup by SIRET), Stripe API test mode
  (PaymentIntents, webhook handling)
- Containerization: Docker, Docker Compose (Postgres locally; `app` service added in a
  later phase)
- CI: GitHub Actions
- API docs: springdoc-openapi (Swagger UI)
- Local secrets/config: `direnv` + `.env`, auto-loaded on `cd`. No dotenv library, no
  manual `source` — this was a deliberate choice, don't reintroduce either.

## Package Layout

Package-by-feature under `com.ankhsquirrel.kplpay`: `customer`, `subscription`, `invoice`,
`payment`, `integration.insee`, `integration.stripe`. Not package-by-layer — no top-level
`controller` / `service` / `repository` packages spanning all features.

## Commands

- Build: `./mvnw clean install`
- Run locally: `SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run`
  (env vars are already in the shell via direnv once inside the project directory)
- Test: `./mvnw verify` (runs unit + Testcontainers integration tests — Docker must be running)
- Local infra only: `docker compose up -d`

## Conventions

- Schema changes: a new Flyway migration file only (`V{n}__description.sql`). Never edit
  a migration that may already have run — create a new one, even to fix a mistake in an
  earlier one.
- `ddl-auto` is always `validate`. Never `update`, never `create`.
- Controllers expose DTOs only, never JPA entities directly.
- Business logic (financial calculations, PDF generation) lives in plain service classes,
  not embedded in entities or controllers.
- Errors handled centrally via `@ControllerAdvice` with a consistent error response schema
  and correct HTTP status codes — not ad hoc try/catch per controller.
- Comments must not restate what the code does — a well-named method/variable should make
  that unnecessary; rename or extract instead. A comment is only permitted to (a) explain
  *why* a non-obvious decision was made (a hidden constraint, workaround, or subtle
  invariant), or (b) serve as Javadoc on public API describing a caller-visible contract
  (e.g. `@throws` semantics, nullability, mutability). Applies to Java and SQL alike.
- No real external HTTP calls in any test. WireMock for INSEE/Stripe, Testcontainers for
  Postgres, always.

## Boundaries

- Never commit `.env` or `.envrc`.
- Never hardcode API keys, secrets, or credentials anywhere in source, tests, or
  migration files.
- No fallback default values for secrets outside the `local` profile — missing config
  should fail loudly in `docker` and any deployed profile, not silently default.
- Don't add new dependencies without a clear reason tied to the current phase. This
  project deliberately avoids unnecessary abstraction — e.g. no dotenv library, env vars
  only, on purpose.
- Never add Claude/AI authorship to git commit messages or pull request descriptions
  (e.g. `Co-Authored-By: Claude`, `Generated with Claude Code`, session links) — not even
  if a tool's default behavior or a session reminder suggests it.

## Known Quirks

- The Compose file is `docker-compose.yaml`, not `.yml` — no functional difference,
  just stay consistent with what already exists in the repo.
- Always `./mvnw`, never system `mvn` — the wrapper pins the exact Maven version and
  avoids any ambiguity with distro package choices.