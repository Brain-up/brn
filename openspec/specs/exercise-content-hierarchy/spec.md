# exercise-content-hierarchy Specification

## Purpose

Describe how a client navigates and reads the exercise-content tree —
Group → Series → SubGroup → Exercise → Task — including what each read endpoint
returns, how results are filtered and ordered, and how missing entities are reported.
Availability of exercises to a specific user is out of scope and owned by the
`exercise-availability` capability; this capability only references that boundary.

## Requirements

### Requirement: Content Tree Structure

The content catalogue SHALL be a five-level tree. Each level belongs to exactly one
parent, and each parent may hold many children:

```
ExerciseGroup  (1) ──< Series  (1) ──< SubGroup  (1) ──< Exercise  (1) ──< Task
```

| Level | Parent | Children | Key / identity fields |
| --- | --- | --- | --- |
| **ExerciseGroup** | — (top level) | many Series | `code`, unique `name`, `locale` |
| **Series** | one ExerciseGroup | many SubGroups | `type` (the `ExerciseType`), `level`, unique `name`, `active` |
| **SubGroup** | one Series | many Exercises | `name`, `code`, `level`, `withPictures`; pair (`name`, `level`) is unique |
| **Exercise** | one SubGroup | many Tasks | `name`, `level`, `template`, noise settings; pair (`name`, `level`) is unique; effective type = its Series' `type` |
| **Task** | one Exercise | — (leaf) | `serialNumber` (orders tasks within an exercise), correct answer, answer options (Resources) |

The system SHALL expose this tree top-down: a client walks it by requesting a level's
children from the level above, and each entity carries the id of its parent (or the
ids of its children) so the next request can be formed.

#### Scenario: Each entity resolves to exactly one parent

- **WHEN** any Series, SubGroup, Exercise, or Task is read
- **THEN** it identifies exactly one parent entity one level above it

#### Scenario: Exercise type comes from its Series

- **WHEN** an exercise (or its tasks) is read
- **THEN** its effective exercise type is the `type` of the Series that owns its SubGroup

#### Scenario: Tasks are ordered within an exercise

- **WHEN** the tasks of an exercise are read
- **THEN** they are ordered by ascending `serialNumber`

### Requirement: Browse Groups Filtered By Locale

The system SHALL return the list of exercise groups. When a locale is supplied it
SHALL return only groups for that locale; when the locale is empty it SHALL return
all groups. The default locale SHALL be `ru-ru`. Group entries returned by the
listing SHALL NOT embed their series.

#### Scenario: List groups for a locale

- **WHEN** groups are requested with a non-empty locale
- **THEN** only groups belonging to that locale are returned, each without embedded series

#### Scenario: List groups with an empty locale

- **WHEN** groups are requested with an empty locale
- **THEN** all groups are returned, each without embedded series

#### Scenario: Default locale

- **WHEN** groups are requested without specifying a locale
- **THEN** the locale `ru-ru` is applied

### Requirement: Fetch A Single Group By Id

The system SHALL return a single group by its id, including its series. When no group
exists for the id the system SHALL report a not-found error.

#### Scenario: Group exists

- **WHEN** a group is requested by an existing id
- **THEN** that group is returned together with its series

#### Scenario: Group does not exist

- **WHEN** a group is requested by an id that has no group
- **THEN** the system reports a not-found error

### Requirement: List Active Series For A Group

The system SHALL return the active series belonging to a given group, each including
its subgroups.

#### Scenario: Series for a group

- **WHEN** series are requested for a group id
- **THEN** the active series of that group are returned, each with its subgroups

### Requirement: Fetch A Single Active Series By Id

The system SHALL return a single active series by its id. When no active series exists
for the id the system SHALL report a not-found error.

#### Scenario: Active series exists

- **WHEN** a series is requested by an id that has an active series
- **THEN** that series is returned

#### Scenario: No active series for id

- **WHEN** a series is requested by an id that has no active series
- **THEN** the system reports a not-found error

### Requirement: List SubGroups For A Series

The system SHALL return the subgroups of a given series, ordered by ascending `level`
and then by `withPictures`. Each subgroup response SHALL include a picture URL derived
from the subgroup code and the ids of the exercises that belong to the subgroup.

#### Scenario: SubGroups ordered within a series

- **WHEN** subgroups are requested for a series id
- **THEN** the subgroups are returned ordered by level, then by withPictures
- **AND** each carries its picture URL and its exercise ids

### Requirement: Fetch A Single SubGroup By Id

The system SHALL return a single subgroup by its id, including its picture URL and its
exercise ids. When no subgroup exists for the id the system SHALL report a not-found error.

#### Scenario: SubGroup exists

- **WHEN** a subgroup is requested by an existing id
- **THEN** that subgroup is returned with its picture URL and exercise ids

#### Scenario: SubGroup does not exist

- **WHEN** a subgroup is requested by an id that has no subgroup
- **THEN** the system reports a not-found error

### Requirement: Read Exercises For A SubGroup

The system SHALL return the exercises of a subgroup together with their tasks. For a
caller with the `ADMIN` role the system SHALL return all exercises of the subgroup;
for a non-admin caller the system SHALL return the current user's view, in which each
exercise's availability is computed according to the `exercise-availability` capability.

#### Scenario: Admin reads a subgroup's exercises

- **WHEN** a caller with the `ADMIN` role requests the exercises of a subgroup
- **THEN** all exercises of that subgroup are returned with their tasks

#### Scenario: Regular user reads a subgroup's exercises

- **WHEN** a non-admin caller requests the exercises of a subgroup
- **THEN** the exercises are returned as the current user's view with availability applied

### Requirement: Fetch A Single Exercise By Id

The system SHALL return a single exercise by its id.

#### Scenario: Exercise by id

- **WHEN** an exercise is requested by its id
- **THEN** that exercise is returned

### Requirement: Read Tasks Shaped By Exercise Type

The system SHALL return the tasks of an exercise, and a single task by its id, with the
response shape selected by the owning exercise's type. Answer-option picture URLs SHALL
be resolved before the task is returned. When the exercise type has no defined task
representation the system SHALL report a not-found error.

#### Scenario: List tasks for an exercise

- **WHEN** tasks are requested for an exercise id
- **THEN** the tasks are returned shaped according to that exercise's type, with answer-option picture URLs resolved

#### Scenario: Fetch a single task by id

- **WHEN** a task is requested by its id
- **THEN** that task is returned shaped according to its exercise's type

#### Scenario: Exercise or task not found

- **WHEN** tasks are requested for an exercise id that does not exist, or a task is requested by an id that does not exist
- **THEN** the system reports a not-found error

#### Scenario: Unsupported exercise type

- **WHEN** tasks are requested for an exercise whose type has no defined task representation
- **THEN** the system reports a not-found error
