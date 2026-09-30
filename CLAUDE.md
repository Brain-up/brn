# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository layout

This is a monorepo for the BrainUp auditory-training platform, containing three independently built apps:

- **Backend** (repo root) — Kotlin + Spring Boot REST API, built with Gradle. Package root `com.epam.brn`, entry point `src/main/kotlin/com/epam/brn/Application.kt`.
- **`frontend/`** — end-user UI, **Ember.js 4.12 (Octane)** + TypeScript, uses **WarpDrive** (ember-data successor) with a schema-driven data layer. Package manager: **pnpm**.
- **`frontend-angular/`** — admin panel, **Angular 11** + npm.

Each subproject has its own README with details; read them before working in a frontend.

## Backend commands (run from repo root)

Use the Gradle wrapper (`./gradlew` / `gradlew.bat` on Windows).

- Build with tests: `gradlew build`
- Run the app: `gradlew bootRun --args=--spring.profiles.active=dev` (needs a Postgres 13 DB running — see README for the `docker run postgres:13` command)
- Run unit tests: `gradlew test`
- Run a single test class: `gradlew test --tests "com.epam.brn.service.SomeServiceTest"`
- Run integration tests: `gradlew integrationTest` — these are **excluded from `test`/`build`** and run separately against a Postgres Testcontainer.
- Coverage report: `gradlew jacocoTestReport` (output in `build/jacoco/`)
- Lint (Kotlin): `gradlew ktlintCheck` — **a failing ktlint fails the build** (`compileKotlin` depends on it). Auto-fix with `gradlew ktlintFormat`.

Building the project also installs git hooks by pointing `core.hooksPath` to `.githooks` (done automatically by `build.gradle.kts`).

### Verification (agent loop)

Single fast entry point: **`gradlew verify`** (registered in `build.gradle.kts`) — runs `ktlintCheck` + unit `test`, no Docker. Use it as the default gate after backend code changes.

Policy — treat these as binding:

1. After changing Kotlin code, run `gradlew verify` and do **not** consider the task done until it exits 0.
2. For repo/migration/`*IT` changes, also run `gradlew integrationTest` (needs Docker — Postgres Testcontainer). This is a **manual / pre-PR** step, intentionally **not** part of `verify`.
3. If a check fails, fix the **implementation**. Do **not** weaken, skip, or delete a test to make it pass unless the test itself is provably wrong — and if you change a test, say so explicitly and why.
4. Run `gradlew ktlintFormat` before finishing (the ktlint gate fails the build).
5. `gradlew verify` is a fast local gate, not a replacement for CI/Sonar — CI stays the authoritative source of truth before merge.

For a thorough, independent pass on a larger change, delegate to the `verifier` subagent (`.claude/agents/verifier.md`); for the step-by-step routine, use the `verify` skill (`.claude/skills/verify/`).

### Test conventions

- **Standardized test stack: JUnit 5 (runner) + MockK (mocks) + kotest-assertions (assertions).** This is the only sanctioned combination — do not introduce Mockito/mockito-kotlin, AssertJ, Kluent, `kotlin.test`, or JUnit's `org.junit.jupiter.api.Assertions`/`assertThrows`/`assertAll`.
  - Mocks: MockK only (`io.mockk`) — `@MockK`/`@InjectMockKs` with `MockKExtension`, `every { } returns`, `verify { }`, `coEvery`/`coVerify` for suspend. Chosen because the codebase is Kotlin (final-by-default classes, coroutines, `object`s) where MockK is idiomatic and Mockito needs workarounds.
  - Assertions: kotest-assertions only (`io.kotest.matchers.*`, `io.kotest.assertions.*`) — e.g. `x shouldBe y`, `shouldThrow<T> { }`, `x.shouldNotBeNull()`, `assertSoftly { }`.
  - One intentional exception: `integration/service/BrainUpUserDetailsServiceTestIT.kt` keeps JUnit `Assertions.assertThrows(Class)` because it asserts on a dynamic `Class<out Exception>?` with no clean reified kotest equivalent.
- Integration tests live under `src/test/kotlin/com/epam/brn/integration/`, extend `BaseIT`, and are marked `@Tag("integration-test")`. The JUnit config in `build.gradle.kts` excludes this tag from the normal `test` task and includes it only in `integrationTest`.

## Frontend commands

Ember user app (`frontend/`, pnpm):
- `pnpm local` — dev server proxying API to `http://localhost:8081` (local backend)
- `pnpm remote` — dev server proxying to production backend
- `pnpm test:ember` — run tests; `pnpm lint` / `pnpm lint:fix` — lint

Angular admin app (`frontend-angular/`, npm):
- `npm run start:dev` (local backend) / `npm run start` (prod backend)
- `npm run test:start` — tests; `npm run lint` — lint

## Full-stack / Docker

- `docker compose up --build` (or `make start`) brings up backend + frontends. Local Swagger at `http://localhost:8081/api/swagger-ui.html`.
- `make docker_unit_test` runs unit tests inside a container.
- `Makefile` wraps common docker-compose flows (`start`, `stop`, `restart`, `clean`).

## Architecture notes

- **Auth is Firebase-based.** Clients authenticate with Firebase and send a bearer token; `auth/filter/FirebaseTokenAuthenticationFilter.kt` validates it and populates the Spring Security context. Firebase admin credentials are loaded from `src/main/resources/firebase-*.json`.
- **Data seeding on startup:** `service/load/InitialDataLoader.kt` and `FirebaseUserDataLoader.kt` load exercise content and users from CSV/resource files (`src/main/resources/initFiles`). These are excluded from coverage/Sonar as non-testable bootstrap code.
- **Persistence:** Spring Data JPA over Postgres. Schema is managed by **Flyway** migrations in `src/main/resources/db/migration/`. Migration files must be named `V2yearmonthday_taskNumber.sql` (e.g. `V220210804_899.sql`).
- **Standard layering:** `controller` → `service` → `repo`, with `dto` (request/response) and `model` (JPA entities). JPA entity classes are opened for proxying via the `allOpen` plugin (`@Entity`/`@MappedSuperclass`/`@Embeddable`).
- **External integrations:** AWS S3 and Google Cloud Storage for media/pictures; Azure & Yandex TTS for speech generation; GitHub API client for contributor data.
- **Ember data layer:** requests flow through a WarpDrive `RequestManager` pipeline (Auth → API normalization → Fetch → Cache). `BrnApiHandler` normalizes the backend's plain REST responses into JSON:API for the WarpDrive cache; models are JSON schemas under `app/schemas/`, not class-based.

## Versions / toolchain

- JDK **17** toolchain, Kotlin **2.1.20**, Spring Boot **3.5.3** (uses `jakarta.*` namespace).
- Postgres **13** (local dev and Testcontainers).
- Frontend Node **22+**; Ember app uses pnpm, Angular app uses npm.

## Conventions

- Branch names: `#GitHub_TaskNumber-# issue description`, task number in range 0–1999.
- Always run `gradlew ktlintFormat` (or IDEA `Ctrl+Alt+L`) before committing Kotlin.
- Tests use **MockK** for mocks and **kotest-assertions** for assertions — never Mockito, AssertJ, Kluent, `kotlin.test`, or JUnit `Assertions` (see [Test conventions](#test-conventions)).
- Coverage/Sonar exclude `dto`, `model`, `config`, `exception`, data loaders, and TTS config — don't chase coverage on those.
