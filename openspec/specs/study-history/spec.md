# study-history Specification

## Purpose

Record the outcome of each finished exercise attempt for a user (study history),
so the platform can show progress, daily/monthly statistics, and drive exercise
availability. This spec describes the current behavior of saving a single
study-history record.

## Requirements

### Requirement: Save Exercise Execution History

The system SHALL persist a study-history record for the currently authenticated
user when they submit the result of a finished exercise via `POST /study-history`.
The endpoint SHALL be available only to users with the `USER` role.

On success the system SHALL respond with `200 OK` and return the saved record,
including its generated identifier.

#### Scenario: Successful save

- **WHEN** an authenticated `USER` submits a result with all required fields and an existing `exerciseId`
- **THEN** the system associates the record with the current user and the referenced exercise
- **AND** persists it and responds `200 OK` with the saved record populated with a generated `id`

#### Scenario: Referenced exercise does not exist

- **WHEN** the submitted `exerciseId` does not match any exercise
- **THEN** the system does not persist any record
- **AND** responds with a not-found error identifying the missing `exerciseId`

#### Scenario: Missing required field

- **WHEN** any of `exerciseId`, `startTime`, `executionSeconds`, `tasksCount`, `replaysCount`, or `wrongAnswers` is absent
- **THEN** the request is rejected as invalid
- **AND** no record is persisted

#### Scenario: Caller without USER role

- **WHEN** a caller without the `USER` role invokes the endpoint
- **THEN** the request is denied and no record is persisted

### Requirement: Derive Study History Metrics On Save

When saving a study-history record, the system SHALL derive metrics from the
submitted counters rather than trusting caller-supplied metric values. The
quality indices (`repetitionIndex`, `rightAnswersIndex`) SHALL be computed and
stored using the save-time formulas defined by the `exercise-quality` capability;
this spec does not restate those formulas.

#### Scenario: Quality indices are derived, not trusted

- **WHEN** a record is saved with counters and any caller-supplied index values
- **THEN** the system recomputes `repetitionIndex` and `rightAnswersIndex` from the counters per the `exercise-quality` save-time rules and stores those computed values, ignoring any caller-supplied index

#### Scenario: Spent time when end time is provided

- **WHEN** a record is saved with both `startTime` and `endTime`
- **THEN** the system computes the spent time in seconds as the difference between `endTime` and `startTime`

#### Scenario: Spent time when end time is absent

- **WHEN** a record is saved without an `endTime`
- **THEN** the spent time in seconds is left unset

### Requirement: Uniqueness Of A Study History Record

The system SHALL treat the combination of user, exercise, and `startTime` as
unique, so the same attempt cannot be recorded twice.

#### Scenario: Duplicate attempt

- **WHEN** a record is saved with the same user, exercise, and `startTime` as an already-stored record
- **THEN** the system rejects the save and does not create a duplicate record
