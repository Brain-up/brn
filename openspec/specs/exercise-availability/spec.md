# exercise-availability Specification

## Purpose

Decide which exercises inside an opened subgroup a user is allowed to do right
now. Exercises within a subgroup are grouped by name, where a name identifies a
sequence of increasing difficulty; a user unlocks harder exercises by completing
earlier ones well enough. This spec describes the current rules for computing
that availability.

## Requirements

### Requirement: Compute Exercise Availability In A Subgroup

The system SHALL, for a given subgroup and user, return every exercise in the
subgroup together with whether it is currently available to that user. Exercises
SHALL be returned in subgroup order (by level).

#### Scenario: All exercises already completed

- **WHEN** the user has completed every exercise in the subgroup
- **THEN** every exercise in the subgroup is available

#### Scenario: Empty subgroup

- **WHEN** the subgroup contains no exercises
- **THEN** the system returns no exercises and no availability

### Requirement: Privileged Roles See All Exercises As Available

The system SHALL treat every exercise in the subgroup as available for a user
whose roles include `ADMIN` or `SPECIALIST`, regardless of study history.

#### Scenario: Admin or specialist

- **WHEN** the requesting user has the `ADMIN` or `SPECIALIST` role
- **THEN** all exercises in the subgroup are marked available

### Requirement: Progressive Unlocking For Regular Users

For a user without a privileged role, the system SHALL compute availability per
name group (exercises sharing the same name, ordered by difficulty). Within each
name group the system SHALL:

- always make the first exercise available as the entry point;
- make every already-completed exercise of that name available;
- unlock the next not-yet-completed exercise only when the last completed exercise
  in that name group was done well **at least once** (in any recorded attempt, not
  only its most recent one).

Unlocking is sticky: once the next exercise has been unlocked by a successful
attempt, a later unsuccessful attempt on an already-completed exercise SHALL NOT
re-lock it.

Exercises in other name groups SHALL be unaffected by a given name group's progress.

#### Scenario: No completed exercise for a name

- **WHEN** the user has not completed any exercise of a given name
- **THEN** only the first exercise of that name is available

#### Scenario: Last attempt was done well

- **WHEN** the last completed exercise of a name has a most recent attempt that was done well
- **AND** at least one exercise of that name is not yet completed
- **THEN** all completed exercises of that name plus the next not-yet-completed exercise are available

#### Scenario: Last attempt was not done well

- **WHEN** the last completed exercise of a name has a most recent attempt that was not done well
- **AND** no earlier attempt of that exercise was done well
- **THEN** only the completed exercises of that name (plus the always-available first) are available
- **AND** the next not-yet-completed exercise remains locked

#### Scenario: Unlock stays after a later unsuccessful attempt

- **WHEN** the last completed exercise of a name was done well in an earlier attempt
- **AND** its most recent attempt was not done well
- **AND** at least one exercise of that name is not yet completed
- **THEN** all completed exercises of that name plus the next not-yet-completed exercise remain available

#### Scenario: No recorded attempt for the last completed exercise

- **WHEN** the last completed exercise of a name has no recorded attempt
- **THEN** the completed exercises of that name are available
- **AND** no further exercise of that name is unlocked

### Requirement: Exercise Quality Threshold

The system SHALL judge whether an attempt was "done well" from its counters,
using two indices that must each meet a configured minimum:

- `repetitionIndex = tasksCount / (replaysCount + tasksCount)`
- `rightAnswersIndex = 1 - (wrongAnswers / tasksCount)`

An attempt is done well only when `repetitionIndex >= minRepetitionIndex` AND
`rightAnswersIndex >= minRightAnswersIndex`, where the minimums are configured
values.

#### Scenario: Both indices meet the minimums

- **WHEN** an attempt's repetition index and right-answers index are both at or above their configured minimums
- **THEN** the attempt is considered done well

#### Scenario: An index is below its minimum

- **WHEN** an attempt's repetition index or right-answers index is below its configured minimum
- **THEN** the attempt is not considered done well

### Requirement: Consistent Availability Across Access Paths

The system SHALL compute the same exercise availability for a given user and
subgroup regardless of which access path is used to obtain it — the subgroup
exercise listing or the available-exercise-ids lookup. Both paths SHALL apply the
availability rules defined in this capability identically.

#### Scenario: Both paths agree for a regular user

- **WHEN** a regular user's availability for a subgroup is obtained via the subgroup exercise listing
- **AND** the available-exercise-ids lookup is obtained for the same user and subgroup
- **THEN** the set of exercises marked available by the listing equals the set of exercise ids returned by the lookup

#### Scenario: Both paths agree for a privileged role

- **WHEN** an `ADMIN` or `SPECIALIST` obtains availability for a subgroup via either access path
- **THEN** both paths report every exercise in the subgroup as available
