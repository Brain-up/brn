## Why

User analytics (the `/admin/users` list and the per-user statistics pages) are computed by aggregating raw `study_history` on every request. #2971 removed the N+1 there with batch queries, but re-aggregating raw history on read does not scale to longer horizons (monthly/yearly trends) or to near-real-time dashboards. The intended direction is to serve analytics from a small precomputed store that is cheap to read and kept fresh as users train. A volunteer change (#2609) introduced a nightly job and a `user_analytics` table, but it stored cumulative all-time totals stamped with a date (a snapshot), which does not roll up into periods and makes incremental freshness impractical. This change defines the store we actually want.

## What Changes

- Introduce a **per-day delta** store (`user_daily_analytics`): one row per user/role/day holding only that day's activity — all completions (`done_exercises`), distinct exercises (`unique_done_exercises`), wall-clock seconds (`spent_time`), active-execution seconds (`execution_time`), and first/last activity that day. Days with no activity produce no row.
- Weekly/monthly/yearly **and all-time (lifetime)** figures are **derived roll-ups** over the daily deltas (`SUM` of time/exercises, `COUNT` of active days, `MIN`/`MAX` activity) — no separate per-period or lifetime tables are introduced; they are read as views/queries and only materialized later if measurements justify it.
- Keep the data fresh **incrementally**: when an exercise is completed (a `study_history` row commits), an asynchronous after-commit listener applies an atomic increment to today's daily-delta row.
- Repurpose the nightly job from *filler* to **reconciler**: once a night it recomputes each affected day's delta from raw `study_history` and overwrites it, self-healing drift from missed or duplicated events.
- The whole precompute path is behind a **single feature flag, on by default** so the store accumulates for validation; the flag can disable the whole path in any environment. The live `/admin/users` endpoint stays realtime (batch queries from #2971); moving it onto this store is a later product decision, out of scope here.

## Capabilities

### New Capabilities
- `user-analytics-precompute`: A precomputed study-analytics store of per-day deltas maintained outside the request path — kept fresh by incremental after-commit updates and reconciled nightly — so period and all-time analytics can be read as plain roll-ups without re-aggregating raw study history.

### Modified Capabilities
<!-- None. This change adds a parallel precomputed store; it does not change the behaviour of the existing user-statistics endpoints, which keep reading study_history directly until a separate change migrates them. -->

## Impact

- **New** `model` entity `UserDailyAnalytics`; repository; a Flyway migration (`V2yearmonthday_2609.sql`) creating the `user_daily_analytics` table.
- **New** after-commit event + async listener triggered on `study_history` persistence (exercise completion path in the study-history/exercise service).
- **Reworked** nightly job (daily-delta recompute/reconcile) replacing the cumulative-snapshot job from #2609.
- **New** feature flag `brn.user.analytics.precompute.enabled` (on by default) gating both the listener and the job beans.
- No change to existing `user-statistics` or `/admin/users` request behaviour; this store is additive and non-realtime until a later change consumes it.
- Relies on the existing `study_history (user_id, start_time)` index (added in `V220260227_2796.sql`) for the nightly recompute.
