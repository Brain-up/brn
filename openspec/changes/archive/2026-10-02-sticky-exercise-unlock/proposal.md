## Why

Today the next exercise in a name group unlocks only while the **most recent**
attempt of the last completed exercise was done well. Replaying an
already-cleared exercise unsuccessfully therefore **re-locks** the exercise it had
unlocked — progress visibly regresses. Users expect progression to be sticky:
once you clear an exercise well, the next one stays open even if you later replay
something badly.

## What Changes

- **BREAKING (behavioural):** Unlocking the next not-yet-completed exercise of a
  name group now depends on whether the last completed exercise of that name was
  done well **at least once in its history**, not on whether its most recent
  attempt was done well. A later unsuccessful attempt no longer re-locks it.
- The availability core stops judging the last attempt's counters and instead
  consumes a precomputed set of "ever done well" exercise ids.
- Data access is unified: both access paths read **all** attempts for the subgroup
  and user via one new repository query and derive the "ever done well" set in
  memory with `ExerciseSuccessCalculator`. The two last-attempt queries that only
  served availability (`findLastAttemptBySubGroupAndUserAccount`,
  `findLastBySubGroupAndUserAccount`) are removed as superseded.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `exercise-availability`: the "Progressive Unlocking For Regular Users"
  requirement changes its unlock condition from "most recent attempt of the last
  completed exercise was done well" to "the last completed exercise was done well
  at least once", making the unlock sticky across later unsuccessful attempts.

## Impact

- `src/main/kotlin/com/epam/brn/service/ExerciseService.kt` — both
  `getAvailableExercisesForSubGroup` and `getAvailableExerciseIds` and the shared
  `computeAvailableExerciseIds` core.
- `src/main/kotlin/com/epam/brn/repo/StudyHistoryRepository.kt` — add
  `findAllAttemptsBySubGroupAndUserAccount`; remove the two superseded
  last-attempt queries.
- `src/test/kotlin/com/epam/brn/service/ExerciseServiceTest.kt` — availability
  scenarios move from "last attempt" to "ever done well", plus a sticky
  (unsuccessful-replay-after-clear) scenario.
- No endpoint, request/response, role, or database-schema changes.
