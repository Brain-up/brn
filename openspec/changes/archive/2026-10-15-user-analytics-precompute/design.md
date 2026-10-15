## Context

See `proposal.md` — Why. The relevant current state:

- Exercise completion is persisted in `StudyHistoryService.save()` (`src/main/kotlin/com/epam/brn/service/StudyHistoryService.kt:34`): it builds a `StudyHistory` row (user, exercise, `startTime`, `endTime`, executionSeconds, etc.) and saves it. This is the single write point we hook for freshness.
- Realtime analytics read `study_history` directly. `/admin/users` (`UserAnalyticsServiceImpl`) and the `/v2/statistics/study/*` endpoints aggregate it per request; #2971 already reduced the `/admin/users` path to 3 batch queries. We are **not** touching these read paths here.
- The volunteer change (#2609/#2687) added a nightly `@Scheduled` job plus a `user_analytics` table whose rows hold cumulative all-time totals stamped with a date. That grain cannot roll up into periods and cannot be incremented safely, so it is superseded by this design.
- `study_history (user_id, start_time)` is already indexed (`V220260227_2796.sql`); the nightly recompute and any day-range scan rely on it.
- Postgres 13 + Flyway; migrations named `V2yearmonthday_taskNumber.sql`.

## Goals / Non-Goals

**Goals:**
- Define a precomputed store whose daily grain is a **per-day delta** so periods are plain `SUM`/`COUNT` roll-ups.
- Keep it fresh on the completion path without adding latency, and self-heal drift nightly.
- Make correctness independent of freshness: a dropped/duplicated increment must not permanently corrupt the store.
- Ship additively: the store is populated but has no reader, so nothing user-visible changes until a later change consumes it; a single flag can disable the whole path.

**Non-Goals:**
- Migrating `/admin/users` or `/v2/statistics/*` onto this store (separate product decision).
- Materializing weekly/monthly/yearly tables or an all-time (lifetime) row — periods and lifetime are derived from the daily deltas until a measured need appears.
- Read/API endpoints over the new store — this change builds the store and keeps it correct; exposing it is later work.

## Decisions

### D1. Daily grain is a per-day DELTA, not a dated cumulative snapshot

Each `user_daily_analytics` row = one user/calendar-day, holding **only that day's** activity: `done_exercises` (all completions), `unique_done_exercises` (distinct exercises), `spent_time` (seconds, `end-start`), `execution_time` (seconds, active execution), and `first_done`/`last_done` within the day. Unique on `(user_id, day)`. See D6 for the column semantics; role is deliberately **not** part of the grain (see Risks).

Why: deltas compose. Period totals = `SUM(spent_time)`, `SUM(done_exercises)`, `COUNT(*)` active days, `MIN(first_done)`/`MAX(last_done)` over the day rows in range — one grain answers week/month/year. A dated cumulative snapshot (the #2609 shape) double-counts when summed and can't be incremented, which is exactly why it's replaced. `study_days` is **not** stored; it is `COUNT(*)` of day rows in the period.

Alternative considered: keep snapshots and diff adjacent dates to recover deltas. Rejected — fragile across gaps/backfills and still un-incrementable.

### D2. All-time per-user values are DERIVED from the daily deltas, not stored

Lifetime figures — `first_study`, `last_study`, `total_time`, `total_exercises` — are a plain aggregation over a user's day rows and are **not** materialized:

```
SELECT MIN(first_done), MAX(last_done), SUM(spent_time), SUM(done_exercises)
FROM user_daily_analytics WHERE user_id = :u
```

Why not a separate `user_lifetime_analytics` running row: the daily grain already carries everything lifetime needs, so a maintained all-time row is pure read-optimization (O(1) vs. an indexed aggregate over a bounded number of day rows) plus insurance against pruning old day rows. Neither pressure exists while the store is dark — there is no reader, and nothing prunes day rows. Storing it now is a second write path and migration to keep consistent for no measured benefit, and it is inconsistent with treating week/month/year as derived (D1). Same principle as periods: derive on read; materialize later only if a reader measures a need. A duplicated/missed increment therefore can't drift an all-time counter independently of the daily rows the nightly reconcile already heals.

### D3. Freshness via async AFTER_COMMIT listener doing an atomic upsert-increment

On `study_history` persistence, publish a domain event. An `@TransactionalEventListener(phase = AFTER_COMMIT)` `@Async` handler applies, for the completed exercise's calendar day:

```
INSERT INTO user_daily_analytics (user_id, day, done_exercises, unique_done_exercises,
                                  spent_time, execution_time, first_done, last_done)
VALUES (:u, :day, 1, :uniqueInc, :spent, :exec, :ts, :ts)
ON CONFLICT (user_id, day)
DO UPDATE SET done_exercises        = user_daily_analytics.done_exercises + 1,
             unique_done_exercises  = user_daily_analytics.unique_done_exercises + EXCLUDED.unique_done_exercises,
             spent_time             = user_daily_analytics.spent_time + EXCLUDED.spent_time,
             execution_time         = user_daily_analytics.execution_time + EXCLUDED.execution_time,
             first_done             = LEAST(user_daily_analytics.first_done, EXCLUDED.first_done),
             last_done              = GREATEST(user_daily_analytics.last_done, EXCLUDED.last_done);
```

`done_exercises` always grows by 1 (every completion counts). `unique_done_exercises` grows by `:uniqueInc`
— the handler sets it to 1 only when this is the first completion of that exercise by that user on that day
(checked with `COUNT(*)` over `study_history` for the day), else 0; distinctness can't be derived from the
upsert alone. The daily row is the only thing written; lifetime figures derive from it on read (D2).

Why AFTER_COMMIT: the delta must reflect only durably committed history; firing in-transaction would count rows that may roll back. Why `@Async`: completion latency is unaffected. Why `ON CONFLICT DO UPDATE` with arithmetic in SQL (not read-modify-write in Kotlin): it is a single atomic statement, so concurrent completions for the same user/day cannot lose updates — no row locks held across a round-trip, no lost-update race.

Alternatives considered:
- *Synchronous in-transaction update* — adds latency and couples completion success to analytics writes. Rejected.
- *Read-modify-write in the service* — classic lost-update under concurrency. Rejected in favour of the atomic upsert.
- *Message queue / outbox* — more moving parts than warranted while the store is dark; the nightly reconciler (D4) already covers lost events. Revisit only if async-listener loss proves material.

### D4. Nightly job is a RECONCILER, not a filler

Once a night the job recomputes a trailing window of finalized days straight from `study_history` (GROUP BY user/day) and overwrites the stored rows. Concretely it deletes then re-inserts every day in `[current_date - reconcileDays, current_date)` (window size `brn.user.analytics.precompute.reconcile-days`, default 7); **today** is deliberately left out and owned by the incremental path (D3). Incremental updates provide freshness; the nightly recompute provides correctness, healing any drift from missed or duplicated events on recent days. The recompute is idempotent — overwrite-from-source yields the same result however many times it runs.

Why a bounded trailing window rather than only "yesterday" or full history: a single-day pass can't heal drift that lands more than a day late (delayed restart, an event dropped two days ago), while a full-history rescan is wasteful once old days are settled. A trailing window heals any day that's still plausibly in flux and leaves settled days untouched; the scan is bounded by the `(user_id, start_time)` index. Days older than the window are treated as final (seeded once by the corrective backfill, D6).

### D5. Single feature flag, on by default

A single flag (`brn.user.analytics.precompute.enabled`, default `true`) gates both the listener bean and the job bean via `@ConditionalOnProperty`. With it off, no event handling and no scheduled run occur, and the completion path and existing endpoints behave exactly as before.

Why on by default: the immediate goal is to **accumulate** the store and validate it against the live aggregations before any read path is switched over, so it must be populating without per-environment opt-in; the nightly job already shipped enabled by default in `#2609`. The store stays dark in the sense that **no reader consumes it** — writing it changes nothing user-visible, so defaulting on carries no user-facing risk. The flag remains so any environment can disable the whole path with one switch.

Why a single flag rather than splitting the job and the listener: one switch keeps "the precompute path" a single operational concept. The listener's only extra risk over the job was the `@Async` + `@TransactionalEventListener` context-load failure, which is resolved by the two-bean split (D3); its writes are after-commit, async, tolerated-on-failure, and healed by the nightly reconcile, so it is safe to enable alongside the job.

### D6. Column semantics: count all completions, keep distinct and both time fields separately

The store mirrors the metrics the product already exposes, so a reader can be switched onto it without changing what users see:

- `done_exercises` = **all** completions that day (repeats included). The product measures "how much the user trained" — 10 completions of one exercise count as 10, matching the user-facing daily table's `allDoneExercises`.
- `unique_done_exercises` = **distinct** exercises *that day* (matches the user-facing daily table's `uniqueDoneExercises`). Note this is per-day distinct; it does **not** sum to a lifetime distinct count (see D7).
- `spent_time` = `SUM(spent_time_in_seconds)` (wall-clock `end-start`) — what the user-facing daily detail table and `/admin/users` sum.
- `execution_time` = `SUM(execution_seconds)` (active execution time) — what the week/month/year statistics graphs (`UserDay`/`UserMonthStatisticsService`) sum. Stored separately because the two time fields differ and different read paths use different ones.

Why both counts and both times are stored rather than derived: they come from different `study_history` columns / different aggregations, so neither can be computed from the other on read. The nightly reconcile (D4) is the source of truth for all four (`count(*)`, `count(distinct)`, `sum(spent_time_in_seconds)`, `sum(execution_seconds)`); the incremental path keeps `done_exercises`/`spent_time`/`execution_time` exactly fresh intraday and `unique_done_exercises` best-effort-fresh (first-today check), with the night settling any drift.

**Corrective recompute of history:** the earlier backfill (`V220261011_2609.sql`) ran with `count(distinct)` for `done_exercises`, without the two new columns, and on the old per-`(user, role, day)` grain; those migrations are immutable (Flyway checksums). A new migration (`V220261013_2609.sql`) documents the time units, adds the columns, drops `role_name` from the grain (dropping the old unique constraint/index and adding `unique (user_id, day)`), and recomputes every finalized day (`day < current_date`) with the corrected formula, so historical rows match the new semantics.

### D7. Which read paths a per-day delta store can serve (phase-3 targeting)

Not every existing read path is reconstructable from per-day deltas. A metric composes across days only if it is additive (`SUM`) or an extremum (`MIN`/`MAX`); a **lifetime distinct count does not** — distinctness doesn't survive day bucketing. This decides where the store is a drop-in replacement and where it is not:

- **Period statistics graphs** (`UserDayStatisticsService`, `UserMonthStatisticsService`, year) — **fully derivable**. Each point is `SUM(execution_seconds)` per day/month (→ `execution_time` / `SUM(execution_time)`), `exercisingDays` = `COUNT(*)` of day rows, dated at the earliest start (→ `MIN(first_done)`). Their `progress` is a pure function of the per-day execution sums and the active-day set (threshold bucketing in `Day`/`WeekExercisingStatusRetriever`), so equal drivers imply equal `progress`. Verified by `UserDailyAnalyticsPeriodEquivalenceIT`. **This is the clean phase-3 switch target.**
- **`/admin/users`** (`UserAnalyticsServiceImpl.getUsersWithAnalytics`) — **mostly** derivable: `spentTime` (`SUM(spent_time)`), `firstDone`/`lastDone` (`MIN`/`MAX`), `studyDaysInCurrentMonth` (`COUNT(*)` rows in month), `lastWeek` (per-day execution + progress). **Except** `doneExercises`, which is `COUNT(DISTINCT exercise)` over **all time** — a lifetime distinct count that neither `SUM(done_exercises)` (all completions) nor `SUM(unique_done_exercises)` (per-day distinct summed) reproduces. So `/admin/users` cannot be fully served from the store without either leaving that one field on `study_history` (little win — the query stays) or a product decision to change the metric. **Not a phase-3 target for now.**
- **User-facing daily detail table** (`StudyHistoryService.calculateUserDailyDetailStatistics`) — out of scope regardless (per-series grain + non-additive "success from first time"; see Non-Goals).

## Risks / Trade-offs

- **Async event loss silently drifts the store** → nightly reconcile (D4) overwrites from source; drift is bounded to at most one day and self-heals.
- **Role is not in the store (grain is `(user_id, day)`)** → a `study_history` row carries no role, and role is a mutable, possibly multi-valued property of the user. Keying by `(user, role, day)` fanned each day's activity out per role (double-counting multi-role users on any cross-role aggregation) and let a role change retroactively split a user's history. No write-time reader needs role, so it is dropped: a reader that wants a role cohort (e.g. `/admin/users`) selects the user set by their **current** role and aggregates the day rows by `user_id`, which matches how the live path already filters (`findUsersAccountsByRole`).
- **Clock/timezone for "calendar day"** → day bucketing must use one consistent zone (server/DB zone, matching how `study_history.start_time` and the existing day-based stats are interpreted) in both the incremental upsert and the nightly recompute, or the two disagree. Pin the zone in one place and reuse it.
- **`@Async` executor configuration** → needs a bounded pool with a sane rejection policy; rejections become dropped increments (tolerated via D4, but should be logged).
- **Backfill of existing history** → enabling the flag does not retro-populate; the first nightly reconcile (or a one-off backfill over the desired range) seeds historical days. Call out at rollout.

## Migration Plan

Rollout order (each step independently shippable behind the flag):

1. **Schema** — Flyway migration `V2yearmonthday_2609.sql` creating `user_daily_analytics`; `V220261013_2609.sql` then finalizes the grain to unique `(user_id, day)` (role dropped) and adds the `unique_done_exercises`/`execution_time` columns.
2. **Incremental listener** — domain event published from `StudyHistoryService.save()`, async AFTER_COMMIT handler doing the atomic daily upsert (D3), gated by the flag.
3. **Reconciler** — rework the nightly job into a trailing-window recompute/overwrite reconcile over `[current_date - reconcileDays, current_date)` (D4), gated by the flag.
4. **Read views/queries** for week/month/year and all-time (lifetime) as plain roll-ups over the daily table — added when a consumer needs them (out of scope to expose here).

Rollback: flag off disables listener + job with no schema change required; the table can remain empty/unused. Dropping it is a follow-up migration if the approach is abandoned.
