## Context

`unify-exercise-availability` (archived) consolidated both availability entry
points onto a single private core, `ExerciseService.computeAvailableExerciseIds`.
That core unlocks the next exercise of a name group when the **last attempt** of
the last completed exercise was done well. The last attempt is all the data the
two availability queries expose: both `findLastBySubGroupAndUserAccount` (entity
path) and `findLastAttemptBySubGroupAndUserAccount` (projection path) select only
the row with `max(startTime)` per exercise+user.

The new rule needs a different fact — "was this exercise **ever** done well" —
which the last-attempt queries cannot answer.

## Goals / Non-Goals

**Goals:**
- Unlock the next exercise when the last completed exercise of a name was done
  well in any recorded attempt; never re-lock it on a later bad attempt.
- Keep both access paths (entity and projection) computing identical availability.
- Keep a single quality judgement (`ExerciseSuccessCalculator`).

**Non-Goals:**
- Changing the quality-threshold formula, endpoints, request/response shapes,
  roles, or the database schema.
- Changing the "first always available" / "all done available" rules or the
  privileged-role short-circuit.

## Decisions

**1. Feed the core a precomputed "ever done well" id set instead of last-attempt
counters.** `computeAvailableExerciseIds` currently takes
`Map<Long, LastAttemptCounters>` and judges it with the calculator. It becomes
`Set<Long> everPassedExerciseIds` and the unlock gate turns into
`lastDoneExercise.id in everPassedExerciseIds`. The success judgement moves out of
the core to its callers, so the core no longer depends on
`ExerciseSuccessCalculator` and the `LastAttemptCounters` type is removed.

Gate correctness: the gate still keys on the **last completed** exercise of the
name group (`currentDoneExercises.last()`), so clearing exercise N (ever) unlocks
N+1, while a done-but-never-passed exercise still does not unlock its successor.
Stickiness follows because "ever done well" cannot be undone by a later attempt.

**2. One new repository query over all attempts, shared by both paths.** Add
`findAllAttemptsBySubGroupAndUserAccount(subGroupId, userId): List<ExerciseLastAttemptView>`
— the same projection as the last-attempt query but without the `max(startTime)`
filter, returning every attempt's counters. Each caller maps it to
`everPassedExerciseIds` by keeping exercise ids with at least one attempt that
`exerciseSuccessCalculator.isSuccessful(...)`. Both paths now read the same source,
which makes the cross-path consistency invariant hold by construction.

**3. Remove the two superseded last-attempt queries.**
`findLastAttemptBySubGroupAndUserAccount` and `findLastBySubGroupAndUserAccount`
are used only by the availability methods being changed (confirmed by grep). After
switching to the all-attempts query they are dead, so they are removed rather than
left as unused JPQL. Their existing test mocks are replaced by mocks of the new
query.

**4. Reuse the `ExerciseLastAttemptView` projection.** It already exposes
`exerciseId`, `tasksCount`, `wrongAnswers`, `replaysCount` — exactly the per-row
counters the new query returns. No new projection type is introduced; the name is
slightly broader than "last attempt" now but the shape is identical, avoiding a
redundant interface.

## Risks / Trade-offs

- **Reading all attempts instead of one row per exercise** is more rows, but
  scoped to a single subgroup+user and only the four counter columns; availability
  is already an interactive, per-subgroup read. Acceptable.
- **The projection name `ExerciseLastAttemptView` no longer only means "last
  attempt."** Mitigated by this design note; renaming it is out of scope and would
  widen the diff into `study-history`/analytics usages.
- **Behaviour change is observable** by clients and existing tests → the
  `exercise-availability` spec is updated in this change and the characterization
  tests are rewritten from "last attempt" to "ever done well" plus a sticky
  scenario.
