## Why

Exercise availability inside a subgroup is currently computed by two separate
implementations that are meant to behave identically but can silently drift:
`getAvailableExercisesForSubGroup` (works on `Exercise` entities, used by the
subgroup exercise listing) and `calculateAvailableExerciseIds` (works on
projections, used by the available-exercise-ids endpoint). Duplicated logic for
the same rules is a maintenance and correctness hazard: a fix applied to one
path can be forgotten in the other, so two callers can disagree about which
exercises a user may do.

## What Changes

- Consolidate the two availability algorithms into a single source of truth so
  both entry points share one implementation of the rules described in the
  `exercise-availability` spec.
- Keep both public entry points and their return shapes (`Set<Exercise>` for the
  subgroup listing, `List<Long>` of ids for the available-ids endpoint); only the
  internal computation is unified.
- Add an explicit spec requirement that availability is consistent across access
  paths, so the guarantee is testable and protected against future drift.
- No change to externally observable availability results, request/response
  shapes, endpoints, roles, or the database.

## Capabilities

### New Capabilities

<!-- none -->

### Modified Capabilities

- `exercise-availability`: add a requirement that, for the same user and
  subgroup, the computed availability is identical regardless of which endpoint
  is used to obtain it. This documents and enforces the invariant the unified
  implementation provides; it does not change the availability rules themselves.

## Impact

- Code: `com.epam.brn.service.ExerciseService` — merge
  `getAvailableExercisesForSubGroup` and `calculateAvailableExerciseIds` onto one
  shared internal routine; the two public methods become thin adapters over their
  respective inputs (`Exercise` vs `ExerciseAvailabilityView` /
  `ExerciseLastAttemptView`).
- Tests: characterization/unit tests asserting both entry points satisfy the
  `exercise-availability` spec, including the cross-path consistency invariant.
- No API, DTO, endpoint, role, migration, or configuration changes.
- Out of scope (tracked separately): the unsafe `ExerciseService.updateActiveStatus`
  `Optional.get()` (returns 500 instead of 404) — different behavior, own change.
