## 1. Lock current behavior with characterization tests

- [ ] 1.1 In `ExerciseServiceTest`, add cases that drive `getAvailableExercisesForSubGroup` through every `exercise-availability` scenario (none done, last attempt done well, last attempt not done well, last attempt missing, all done) and verify the returned `Set<Exercise>` matches today's output. Verify with `gradlew test --tests "com.epam.brn.service.ExerciseServiceTest"`.
- [ ] 1.2 Add matching cases that drive `calculateAvailableExerciseIds` through the same scenarios and verify the returned `List<Long>` order and contents match today's output. Verify with the same test command.
- [ ] 1.3 Add a cross-path consistency test: for the same subgroup exercises, done ids, and last attempts, assert the ids marked available by the entity path equal the ids returned by the projection path — for both a regular user and a privileged (`ADMIN`/`SPECIALIST`) role. Verify the new test fails only if the two paths diverge and passes against current code.

## 2. Extract the shared quality judgement

- [ ] 2.1 Add a private core `isDoneWell(tasksCount, wrongAnswers, replaysCount)` on `ExerciseService` implementing the single quality-threshold formula from the `exercise-availability` spec. Verify with `gradlew test --tests "com.epam.brn.service.ExerciseServiceTest"`.
- [ ] 2.2 Make the public `isDoneWell(studyHistory: StudyHistory)` a thin delegator to the core, preserving its signature and result. Verify `UserAnalyticsServiceTest` stays green (`gradlew test --tests "com.epam.brn.service.UserAnalyticsServiceTest"`).
- [ ] 2.3 Make the private `isDoneWell(lastAttempt: ExerciseLastAttemptView)` delegate to the core and remove its duplicated formula. Verify no other `isDoneWell` formula remains via `Grep` and that the availability tests from group 1 still pass.

## 3. Extract the shared availability core

- [ ] 3.1 Add a private core routine that takes an ordered list of `(id, name)`, a `Set<Long>` of done ids, and a `Map<Long, lastAttemptCounters>`, and returns the ordered list of available ids, implementing the progressive-unlocking rules (first-of-name always available, done available, next unlocked only if the last done attempt of the name is done well) canonicalized on the projection algorithm. Verify with the group 1 tests.
- [ ] 3.2 Rewrite `calculateAvailableExerciseIds` as a thin adapter that shapes its projections into the core's inputs and returns the core result unchanged. Verify its group 1 characterization tests still pass.
- [ ] 3.3 Rewrite `getAvailableExercisesForSubGroup` as a thin adapter that shapes its `Exercise` entities and `StudyHistory` last attempts into the core's inputs, then maps the returned ids back to the corresponding `Exercise` entities. Verify its group 1 characterization tests still pass.

## 4. Verify and finalize

- [ ] 4.1 Run the full unit suite and confirm no availability behavior changed: `gradlew test`.
- [ ] 4.2 Run `gradlew ktlintFormat` then `gradlew ktlintCheck` and confirm the build's Kotlin lint gate passes with no wildcard/unused imports.
