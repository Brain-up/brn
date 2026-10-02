## 1. Add all-attempts data access

- [x] 1.1 Add `findAllAttemptsBySubGroupAndUserAccount(subGroupId, userId): List<ExerciseLastAttemptView>` to `StudyHistoryRepository` — same projection as `findLastAttemptBySubGroupAndUserAccount` but without the `max(startTime)` filter (every attempt's counters for the subgroup+user). Verify with `gradlew compileKotlin`.

## 2. Make the unlock rule sticky in the shared core

- [x] 2.1 Change `computeAvailableExerciseIds` to take `everPassedExerciseIds: Set<Long>` instead of `Map<Long, LastAttemptCounters>`; replace the last-attempt success check with `lastDoneExercise.id in everPassedExerciseIds`. Remove the now-unused `LastAttemptCounters` type and the core's dependency on `ExerciseSuccessCalculator`. Update the KDoc to state the "ever done well" rule.
- [x] 2.2 In `getAvailableExerciseIds`, build `everPassedExerciseIds` from `findAllAttemptsBySubGroupAndUserAccount` filtered by `exerciseSuccessCalculator.isSuccessful(...)`, and pass it to the core via `calculateAvailableExerciseIds`.
- [x] 2.3 In `getAvailableExercisesForSubGroup`, build `everPassedExerciseIds` the same way from `findAllAttemptsBySubGroupAndUserAccount` and pass it to the core; map the returned ids back to `Exercise` entities.
- [x] 2.4 Adapt `calculateAvailableExerciseIds` to forward the `Set<Long>` to the core.

## 3. Remove superseded queries

- [x] 3.1 Remove `findLastAttemptBySubGroupAndUserAccount` and `findLastBySubGroupAndUserAccount` from `StudyHistoryRepository` once nothing references them. Verify no references remain via `Grep`.

## 4. Update characterization tests

- [x] 4.1 Update `ExerciseServiceTest` availability scenarios to the new rule: last completed exercise ever done well unlocks the next; never done well keeps it locked; no attempt keeps it locked. Replace mocks of the removed queries with mocks of `findAllAttemptsBySubGroupAndUserAccount`.
- [x] 4.2 Add a sticky scenario: an exercise cleared in an earlier attempt and then replayed unsuccessfully still unlocks the next exercise (both access paths).
- [x] 4.3 Keep the cross-path consistency tests green against the new data source.

## 5. Verify and finalize

- [x] 5.1 Run `gradlew verify` (ktlintCheck + unit test) and confirm it exits 0.
- [x] 5.2 Run `gradlew ktlintFormat` and confirm the Kotlin lint gate passes with no wildcard/unused imports.
