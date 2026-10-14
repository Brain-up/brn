## Purpose

Maintain a precomputed study-analytics store of per-day activity deltas, kept fresh as users train and reconciled nightly, so period and all-time analytics can be read as plain roll-ups over the deltas without re-aggregating raw study history on the request path.

## ADDED Requirements

### Requirement: Per-Day Activity Deltas

The system SHALL maintain, for each user and role, at most one record per calendar day that captures only that day's study activity: the number of exercises completed that day counting every completion including repeats, the number of distinct exercises completed that day, the wall-clock time spent that day in seconds, the active execution time that day in seconds, and the first and last activity timestamps of that day. A day on which the user has no study activity SHALL have no record. Each day's figures SHALL be independent of other days (a delta, not a running total), so that a day's record reflects activity on that day alone.

#### Scenario: One record per active day

- **WHEN** a user has study activity on several distinct calendar days
- **THEN** the store holds exactly one record per active day for that user and role, and days without activity have no record

#### Scenario: A day's figures count only that day

- **WHEN** a day's record is produced
- **THEN** its exercises-done (all completions), distinct-exercises, seconds-spent, and execution-seconds reflect only the records whose activity falls on that calendar day
- **AND** a repeated exercise increases exercises-done but not distinct-exercises
- **AND** its first and last activity timestamps are the earliest and latest activity of that day

### Requirement: Period And All-Time Figures Derived From Daily Deltas

The system SHALL derive weekly, monthly, yearly, and all-time (lifetime) figures for a user by combining that user's daily deltas: time spent and exercises done SHALL be the sums of the daily values over the range, the active-day count SHALL be the number of daily records in the range, and first and last activity SHALL be the minimum and maximum across the range's daily records. All-time figures SHALL use the full set of the user's daily records (first-ever and most-recent activity being the overall minimum and maximum). The system SHALL NOT require a separate stored table per period, nor a stored per-user lifetime record, to answer these.

#### Scenario: Weekly and monthly totals sum the daily deltas

- **WHEN** a period figure is requested for a user over a date range
- **THEN** time spent and exercises done equal the sums of the user's daily deltas within that range
- **AND** the active-day count equals the number of the user's daily records within that range

#### Scenario: All-time figures aggregate every daily delta

- **WHEN** an all-time figure is requested for a user
- **THEN** total time spent and exercises done equal the sums over all of the user's daily records
- **AND** first study and most-recent study equal the earliest and latest activity across all of the user's daily records

### Requirement: Incremental Freshness On Exercise Completion

When a study-history record is committed (an exercise is completed), the system SHALL update that user's current-day delta to include the completed exercise, without blocking or delaying the completion response. Concurrent completions for the same user SHALL NOT lose updates. The update SHALL be applied only after the study-history record is durably committed.

#### Scenario: Current-day delta reflects a just-completed exercise

- **WHEN** a user completes an exercise while the precompute path is enabled
- **THEN** that user's current-day delta grows by one completion and by the exercise's spent and execution time
- **AND** the distinct-exercises count grows only when this is the first completion of that exercise that day
- **AND** the completion request itself is not delayed by the update

#### Scenario: Concurrent completions do not lose updates

- **WHEN** a user completes two exercises at nearly the same time
- **THEN** the current-day delta reflects both completions

### Requirement: Nightly Reconciliation

The system SHALL, once per day, recompute each day's delta from the raw study history and overwrite the stored deltas, so that any drift from missed or duplicated incremental updates is corrected. Re-running the reconciliation for a day SHALL be idempotent — it SHALL leave the same stored result as a single run.

#### Scenario: Reconciliation corrects drift

- **WHEN** a day's stored delta disagrees with the raw study history for that day
- **THEN** after reconciliation the stored delta matches the figures recomputed from the raw history

#### Scenario: Reconciliation is idempotent

- **WHEN** the reconciliation runs more than once for the same day
- **THEN** the stored deltas for that day are the same as after a single run

### Requirement: Precompute Path Is Toggleable By Configuration

The precompute store, its incremental updates, and its nightly reconciliation SHALL be controlled by a single configuration flag that is enabled by default, so the store accumulates without per-environment opt-in. When the flag is disabled, no incremental updates and no nightly reconciliation SHALL run, and exercise completion and the existing realtime analytics endpoints SHALL behave exactly as before the feature existed.

#### Scenario: Enabled by default accumulates the store

- **WHEN** the precompute flag is left at its default
- **THEN** the nightly reconciliation runs and completed exercises are folded into the store

#### Scenario: Disabling turns the whole path off

- **WHEN** the precompute flag is explicitly disabled
- **THEN** no incremental updates and no nightly reconciliation run, and exercise completion behaves exactly as before the feature existed
