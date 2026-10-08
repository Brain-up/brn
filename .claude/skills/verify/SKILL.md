---
name: verify
description: Run the backend verification loop after Kotlin/Spring code changes — Kotlin style + unit tests via `gradlew verify`, plus integration tests when repos/migrations changed — and drive fixes until green. Use after editing backend code or when the user asks to verify/check the build.
---

# Verify

The routine for confirming a backend change is correct. Fix the implementation
to satisfy the checks; never weaken the checks to fit the implementation.

## Steps

1. **Scope the change.** Note which files/modules changed. If you touched
   repositories, Flyway migrations (`src/main/resources/db/migration`), or any
   `*IT` test, mark that integration verification is required.

2. **Fast feedback while iterating (optional).** For a quick loop on one area,
   run the narrowest useful target first, e.g.
   `gradlew test --tests "com.epam.brn.service.SomeServiceTest"`.

3. **Run the gate.** `gradlew verify` — this is `ktlintCheck` + unit `test`, no
   Docker. Do not treat the task as done until it exits 0.

4. **On failure, find the root cause.** Read the actual failure (assertion,
   compile error, or ktlint rule). Fix the **implementation**. Only edit a test
   if the test itself is provably wrong — then state explicitly what you changed
   and why. Never delete/skip/`@Disabled` a test just to go green.

5. **Repeat** steps 3–4 until `gradlew verify` passes.

6. **Style.** Run `gradlew ktlintFormat` before finishing; the ktlint gate fails
   the build otherwise. Re-run `gradlew verify` if it reformatted anything.

7. **Integration (only if step 1 flagged it).** Ensure Docker is running, then
   `gradlew integrationTest`. These use a Postgres Testcontainer and are slow —
   run them once at the end, not on every edit.

## Conventions to respect

- Test stack is fixed: JUnit 5 + MockK + kotest-assertions. Do not introduce
  Mockito, AssertJ, Kluent, `kotlin.test`, or JUnit `Assertions` (see CLAUDE.md).
- `gradlew verify` is a local pre-PR gate, not a replacement for CI/Sonar.
