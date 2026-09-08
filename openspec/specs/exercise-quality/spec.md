# exercise-quality Specification

## Purpose

Define how a single exercise attempt is turned into quality measures and judged
"done well". Two derived indices — a repetition index and a right-answers index —
summarize an attempt's counters (`tasksCount`, `replaysCount`, `wrongAnswers`).
These measures underpin other capabilities: `exercise-availability` uses the
done-well judgement to unlock the next exercise, `study-history` persists the
indices with each record, and `user-statistics` reports aggregated progress.

This spec captures the current rules as implemented. It also records a known
divergence: the `repetitionIndex` value persisted on save is computed with a
different formula than the `repetitionIndex` recomputed for the done-well
judgement. Both behaviors are documented here as-is; aligning them is a separate
change.

## Requirements

### Requirement: Derived Quality Indices Recorded On Save

When a study-history record is created, the system SHALL compute and store two
derived indices from the attempt's counters:

- `rightAnswersIndex` = `(tasksCount - wrongAnswers) / tasksCount`;
- `repetitionIndex` = `replaysCount / (tasksCount + replaysCount)`.

Both are stored on the persisted record alongside the raw counters.

#### Scenario: Right-answers index on save

- **WHEN** an attempt with `tasksCount` tasks and `wrongAnswers` wrong answers is saved
- **THEN** the stored `rightAnswersIndex` equals `(tasksCount - wrongAnswers) / tasksCount`

#### Scenario: Repetition index on save reflects replay share

- **WHEN** an attempt with `tasksCount` tasks and `replaysCount` replays is saved
- **THEN** the stored `repetitionIndex` equals `replaysCount / (tasksCount + replaysCount)`

### Requirement: Done-Well Judgement From Attempt Counters

The system SHALL judge an attempt "done well" only when both recomputed indices
meet their configured minimums:

- repetition index = `tasksCount / (replaysCount + tasksCount)`;
- right-answers index = `1 - wrongAnswers / tasksCount`;
- done well when repetition index `>= minRepetitionIndex` AND right-answers index
  `>= minRightAnswersIndex`.

The minimums are configuration values (`minRepetitionIndex`,
`minRightAnswersIndex`), each defaulting to `0.8`. The judgement SHALL be computed
from the attempt's counters, not read from any previously stored index field.

#### Scenario: Attempt meets both thresholds

- **WHEN** an attempt's recomputed repetition index and right-answers index are both at or above their configured minimums
- **THEN** the attempt is judged done well

#### Scenario: Attempt below a threshold

- **WHEN** either recomputed index is below its configured minimum
- **THEN** the attempt is not judged done well

#### Scenario: Right-answers index matches the stored formula

- **WHEN** the same attempt is judged and its right-answers index is stored on save
- **THEN** both use `1 - wrongAnswers / tasksCount` (equivalently `(tasksCount - wrongAnswers) / tasksCount`) and agree

### Requirement: Quality Judgement Is Independent Of The Stored Repetition Index

The done-well judgement's repetition index SHALL be recomputed from the attempt's
counters as `tasksCount / (replaysCount + tasksCount)`. This differs from the
`repetitionIndex` persisted on save (`replaysCount / (tasksCount + replaysCount)`),
which is its complement (`1 - judged repetition index`). Consequently the value
persisted under `repetitionIndex` is NOT the value used to judge whether an
attempt was done well. This divergence is current behavior, recorded here so it is
visible and can be reconciled by a dedicated change.

#### Scenario: Stored repetition index differs from the judged one

- **WHEN** an attempt has a non-zero number of replays
- **THEN** the `repetitionIndex` stored on save and the repetition index used by the done-well judgement are different values (complements summing to `1`)

#### Scenario: Judgement ignores the persisted repetition index

- **WHEN** an attempt is judged done well
- **THEN** the judgement uses the recomputed repetition index and does not read the persisted `repetitionIndex` field
