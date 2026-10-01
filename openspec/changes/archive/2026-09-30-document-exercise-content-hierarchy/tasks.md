## 1. Validate the planning artifacts

- [x] 1.1 Run `openspec validate document-exercise-content-hierarchy --strict` and verify it passes with no errors

## 2. Confirm each requirement against the current code

- [x] 2.0 Verify "Content Tree Structure" against the JPA models — `ExerciseGroup` → `Series` → `SubGroup` → `Exercise` → `Task` associations, the `(name, level)` uniqueness on SubGroup/Exercise, `Task.serialNumber` ordering (`@OrderBy`), and exercise type derived from `Series.type`
- [x] 2.1 Verify "Browse Groups Filtered By Locale" against `GroupController.getGroups` / `ExerciseGroupsService.findByLocale` — confirm default `ru-ru`, empty-locale = all, and `toDtoWithoutSeries` (no embedded series)
- [x] 2.2 Verify "Fetch A Single Group By Id" against `ExerciseGroupsService.findGroupDtoById` — confirm series are included and a missing id throws `EntityNotFoundException`
- [x] 2.3 Verify "List Active Series For A Group" and "Fetch A Single Active Series By Id" against `SeriesService.findSeriesForGroup` (with subgroups) and `findSeriesDtoForId` (active-only, not-found on missing)
- [x] 2.4 Verify "List SubGroups For A Series" and "Fetch A Single SubGroup By Id" against `SubGroupService.findSubGroupsForSeries` (sorted by level then withPictures) and `findById`, including picture URL and exercise ids in the response, and not-found behaviour
- [x] 2.5 Verify "Read Exercises For A SubGroup" and "Fetch A Single Exercise By Id" against `ExerciseController.getExercisesBySubGroup` role branching (admin → all with tasks, non-admin → current-user availability view) and `getExercisesByID`
- [x] 2.6 Verify "Read Tasks Shaped By Exercise Type" against `TaskService.getTasksByExerciseId` / `getTaskById` — confirm type-driven DTO shaping, answer-option picture resolution, and not-found on missing exercise/task and unsupported type

## 3. Back-fill characterization tests for uncovered scenarios

- [x] 3.1 For any scenario in section 2 not already covered by an existing unit/integration test, add a characterization test in the standard stack (JUnit5 + MockK + kotest) and verify `gradlew verify` passes — added `ExerciseGroupsServiceTest` (the only read service with no existing test); Series/SubGroup/Task read paths already covered by existing service tests
- [x] 3.2 If any scenario does not match the code, stop and report the mismatch (spec draft vs. code) rather than editing code silently — record the resolution in the change before proceeding — no mismatches found; spec matches current behaviour

## 4. Finalize

- [x] 4.1 Run `gradlew verify` and confirm it exits 0 (ktlint + unit tests green)
- [x] 4.2 Re-run `openspec validate document-exercise-content-hierarchy --strict` and confirm the change is ready to archive
