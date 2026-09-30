## Why

The read side of the exercise-content tree — how a client walks Group → Series →
SubGroup → Exercise → Task and what each read endpoint returns — is only encoded in
controllers, services, and caches, with no spec. Without a Business Analyst this
tacit knowledge is easy to break silently (ordering, locale filtering, not-found
behaviour, type-driven task shaping). This change documents the **existing**
behaviour as an OpenSpec capability, matching the style of the current specs
(`exercise-availability`, `study-history`, …). It adds no new behaviour.

## What Changes

- Introduce a new capability `exercise-content-hierarchy` describing the read/navigation
  contract over the content tree:
  - **Groups**: list groups filtered by locale (default `ru-ru`, empty locale = all,
    without series), and fetch a single group by id (with series).
  - **Series**: list active series for a group (with subgroups), and fetch a single
    active series by id.
  - **SubGroups**: list subgroups for a series ordered by `level` then `withPictures`,
    and fetch a single subgroup by id; each subgroup response carries its picture URL
    and its exercise ids.
  - **Exercises (read shape)**: fetch a single exercise by id, and list exercises for a
    subgroup — where an admin caller gets all exercises with tasks and a non-admin caller
    gets the current-user view. (The *availability* computation itself stays owned by the
    existing `exercise-availability` spec; this spec only references it at the boundary.)
  - **Tasks**: list tasks for an exercise and fetch a single task by id, with the response
    DTO shape selected by the exercise's `ExerciseType`, and picture URLs resolved for
    answer options.
  - **Not-found and ordering** rules for each read path.
- No API, schema, or code changes. This is documentation of current behaviour only.

## Capabilities

### New Capabilities
- `exercise-content-hierarchy`: navigation and read contract over the
  Group → Series → SubGroup → Exercise → Task tree — ordering, locale filtering,
  per-type task shaping, and not-found behaviour of the read endpoints.

### Modified Capabilities
<!-- None. Availability rules remain owned by exercise-availability; this spec only
     references that boundary and does not change its requirements. -->

## Impact

- **Specs**: new `openspec/specs/exercise-content-hierarchy/spec.md`.
- **Code**: none. Describes existing `SeriesController`/`SeriesService`,
  `GroupController`/`ExerciseGroupsService`, `SubGroupController`/`SubGroupService`,
  `ExerciseController` read paths, and `TaskController`/`TaskService`.
- **Tests / APIs / DB**: unchanged.
