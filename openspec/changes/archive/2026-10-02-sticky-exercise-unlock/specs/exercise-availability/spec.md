## MODIFIED Requirements

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
