# daily-timer Specification

## Purpose

Report how much time a user has spent doing exercises on a given calendar day,
so the app can show a per-day training timer. The headline case is the current
user's timer for today. This spec describes the current rules for computing that
total.

## Requirements

### Requirement: Report A User's Exercise Time For A Day

The system SHALL report, for a user and a calendar day, the total exercise time
recorded that day, in seconds.

The total SHALL be the sum of the execution seconds of every study-history
record whose `startTime` falls on that calendar day for that user. A record
belongs to a day by the calendar date of its `startTime`, regardless of the time
of day. The total SHALL be computed from the recorded execution seconds of each
attempt, not from the elapsed wall-clock time between start and end.

#### Scenario: Multiple attempts on the day

- **WHEN** the user has several study-history records with `startTime` on the day
- **THEN** the reported time is the sum of those records' execution seconds

#### Scenario: No attempts on the day

- **WHEN** the user has no study-history record with `startTime` on the day
- **THEN** the reported time is `0`

#### Scenario: Attempts on other days are excluded

- **WHEN** the user has records on other days as well
- **THEN** only records whose `startTime` falls on the requested day contribute to the total

#### Scenario: Attempt without recorded execution seconds

- **WHEN** a contributing record has no execution seconds recorded
- **THEN** it contributes `0` to the total rather than causing the total to be absent

### Requirement: Today's Timer For The Current User

The system SHALL expose the current user's exercise time for the current date via
`GET /study-history/todayTimer`, returning the total in seconds. The endpoint
SHALL be available only to users with the `USER` role.

#### Scenario: Timer for today

- **WHEN** an authenticated `USER` requests the today timer
- **THEN** the system responds with the sum of that user's execution seconds for records dated the current day

#### Scenario: Fresh day with no activity

- **WHEN** the current user has no study-history record dated the current day
- **THEN** the system responds with `0`

#### Scenario: Caller without USER role

- **WHEN** a caller without the `USER` role invokes the endpoint
- **THEN** the request is denied
