# user-statistics Specification

## Purpose

Aggregate a user's study history into training statistics the app shows over
time: per-day totals across a period, per-month totals across a period, and a
detailed per-series breakdown for a single day. Each aggregated period also
carries a progress status (BAD / GOOD / GREAT). This spec describes the current
rules for computing and returning those statistics.

## Requirements

### Requirement: Per-Day Statistics Over A Period

The system SHALL return per-day study statistics for a `[from, to]` period via
`GET /v2/statistics/study/week`, one entry per day on which the user has study
history. Each entry SHALL carry the day's date, the total exercising time in
seconds, and a progress status. The endpoint SHALL require the `USER` role.

#### Scenario: One entry per active day

- **WHEN** the user has study history on several days within the period
- **THEN** the response contains one entry per day that has history, and days without history produce no entry

#### Scenario: Daily exercising time

- **WHEN** an entry is produced for a day
- **THEN** its exercising time in seconds is the sum of the execution seconds of that day's records

### Requirement: Per-Month Statistics Over A Period

The system SHALL return per-month study statistics for a `[from, to]` period via
`GET /v2/statistics/study/year`, one entry per month on which the user has study
history. Each entry SHALL carry a date in the month, the total exercising time in
seconds, the number of distinct active days that month, and a progress status.
The endpoint SHALL require the `USER` role.

#### Scenario: One entry per active month

- **WHEN** the user has study history across several months within the period
- **THEN** the response contains one entry per month that has history

#### Scenario: Monthly totals

- **WHEN** an entry is produced for a month
- **THEN** its exercising time in seconds is the sum of that month's execution seconds
- **AND** its active-days count is the number of distinct calendar days that month on which the user has history

### Requirement: Detailed Per-Series Statistics For A Day

The system SHALL return a per-series breakdown of a single day's activity via
`GET /v2/statistics/study/day`, one entry per series the user practiced that day.
The endpoint SHALL require the `USER` role. The day spans from the start of the
given date to the end of that date.

Each series entry SHALL report:

- all done exercises: the number of study-history records for that series that day;
- unique done exercises: the number of distinct exercises practiced;
- repeated exercises: all done exercises minus unique done exercises;
- listened words count: the sum of the records' task counts;
- duration: the total spent time for those records, expressed in minutes.

#### Scenario: Series breakdown for a day

- **WHEN** the user practiced exercises from one or more series on the day
- **THEN** the response contains one entry per series with the reported counts and duration

#### Scenario: Repeated exercises

- **WHEN** the user did the same exercise more than once in a series that day
- **THEN** repeated exercises equals all done exercises minus unique done exercises for that series

### Requirement: Admins May Query Another User's Statistics

For every statistics endpoint, the system SHALL compute statistics for a
requested `userId` only when the caller has the `ADMIN` role; otherwise it SHALL
compute statistics for the current user, ignoring any supplied `userId`.

#### Scenario: Admin requests another user

- **WHEN** an `ADMIN` caller supplies a `userId`
- **THEN** the statistics are computed for that user

#### Scenario: Non-admin supplies a userId

- **WHEN** a non-admin caller supplies a `userId`
- **THEN** the supplied `userId` is ignored and statistics are computed for the current user

### Requirement: Exercising Progress Status

The system SHALL attach a progress status of `BAD`, `GOOD`, or `GREAT` to each
aggregated period, derived from configured thresholds for that period type. When
more than one status criterion matches, the system SHALL choose the lowest
status (`BAD` before `GOOD` before `GREAT`).

#### Scenario: Daily progress from time exercised

- **WHEN** a day's total exercising time falls within a configured threshold band for the day period
- **THEN** the day is assigned that band's status

#### Scenario: Weekly progress from activity

- **WHEN** a week's activity, measured from how many of the seven days were not rest days, falls within a configured threshold band
- **THEN** the period is assigned that band's status

#### Scenario: No matching threshold

- **WHEN** no configured threshold band matches the aggregated value
- **THEN** no progress status is assigned for that period
