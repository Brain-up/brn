## Context

See proposal.md — Why. Two methods in `ExerciseService` implement the same
availability rules on different inputs:

- `getAvailableExercisesForSubGroup(doneSubGroupExercises, subGroupExercises, userId, subGroupId): Set<Exercise>`
  — operates on `Exercise` entities; obtains the last attempt via
  `studyHistoryRepository.findLastBySubGroupAndUserAccount` (full `StudyHistory`)
  and judges it with `isDoneWell(StudyHistory)`.
- `calculateAvailableExerciseIds(subGroupExercises, doneExerciseIds, lastAttemptsByExerciseId): List<Long>`
  — operates on `ExerciseAvailabilityView` / `ExerciseLastAttemptView`
  projections; judges the last attempt with `isDoneWell(ExerciseLastAttemptView)`.

Both need only four things per subgroup: exercises in order with `id` and `name`,
the set of done exercise ids, the last attempt's counters
(`tasksCount`, `wrongAnswers`, `replaysCount`) for the last done exercise of each
name, and the two configured thresholds. They differ in incidental input types,
not in rules — the duplication is the risk.

## Goals / Non-Goals

**Goals:**
- One implementation of the availability rules, shared by both entry points.
- One `isDoneWell` computation instead of two identical overloads.
- Behavior identical to today for every existing scenario (spec is the oracle).

**Non-Goals:**
- Changing availability rules or the quality-threshold formula.
- Changing endpoints, request/response shapes, roles, or the database.
- Fixing `updateActiveStatus` or the `repetitionIndex` formula discrepancy
  (separate changes).

## Decisions

**1. Extract a type-agnostic core routine.**
Introduce a private core that takes minimal, primitive-ish inputs — an ordered
list of `(id, name)`, a `Set<Long>` of done ids, and a
`Map<Long, attemptCounters>` for last attempts — and returns the ordered list of
available ids. Both public methods become thin adapters that build those inputs
from their own data and (for the entity path) map the resulting ids back to
`Exercise`. Chosen over a shared generic interface with an adapter per input type
because the inputs reduce cleanly to ids/names/counters, so a small data-shaping
adapter is simpler than introducing an abstraction layer.

**2. Canonicalize the incidental differences on the projection-based version.**
The projection version (`calculateAvailableExerciseIds`) is the cleaner and
already order-preserving one, so it becomes the canonical algorithm:
- "next unlocked" = first not-yet-done exercise in name-group order
  (`firstOrNull { it.id !in doneIds }`), replacing the entity path's
  `minus(doneSubGroupExercises).first()`.
- single "all done" short-circuit by comparing done-id count to subgroup size.
These are believed equivalent to the entity path today; characterization tests
(Decision 4) confirm it before the merge lands.

**3. One quality judgement in a dedicated bean, two thin adapters — but keep both public entry points.**
Judge the last attempt from its counters only, via a single
`ExerciseSuccessCalculator.isSuccessful(tasksCount, wrongAnswers, replaysCount)`.
The threshold formula and the `minRepetitionIndex` / `minRightAnswersIndex`
properties move onto this dedicated bean (rather than a private method on
`ExerciseService`) so the single source of truth is shared cleanly by
`ExerciseService`, `StudyHistoryService`, and `UserAnalyticsServiceImpl` — the
latter now depends on the calculator directly instead of reaching through
`ExerciseService`. The calculator also exposes `StudyHistory` and
`ExerciseLastAttemptView` convenience overloads, so each availability path passes
its own last-attempt representation. The public
`ExerciseService.isDoneWell(StudyHistory)` method MUST stay — it is called
outside this service by `UserAnalyticsServiceImpl` — so it becomes a thin
delegator to the calculator; the former private
`isDoneWell(ExerciseLastAttemptView)` overload is removed and the availability
core calls the calculator directly, eliminating the duplicated formula.
Keep both repository queries (`findLastBySubGroupAndUserAccount` and
`findLastAttemptBySubGroupAndUserAccount`): the former is only called by the
entity path we are refactoring, but it is mocked across many existing
`ExerciseServiceTest` cases, so retaining it keeps the refactor low-churn and
lets those tests act as the characterization net.

**4. Lock current behavior with tests first.**
Before merging, add tests that exercise both entry points across the
`exercise-availability` scenarios (none done, last-attempt done well / not well /
missing, all done, privileged roles) plus the new cross-path consistency
invariant. This makes the refactor observably behavior-preserving.

## Risks / Trade-offs

- **The two implementations may already differ on an edge case** → characterization
  tests over both paths surface any divergence before the merge; where they
  disagree, the `exercise-availability` spec is the arbiter and the resolution is
  called out in the change.
- **The shared core must satisfy the external caller `UserAnalyticsServiceImpl`
  unchanged** → the public `isDoneWell(StudyHistory)` signature and result are
  preserved; its existing tests (`UserAnalyticsServiceTest`) stay green as a
  guard.
- **Reworking the private algorithm could subtly shift results** → both paths keep
  their current repository queries and are covered by the existing
  `ExerciseServiceTest` cases plus the new cross-path consistency test.
