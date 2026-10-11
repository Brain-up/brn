
-- #2609 One-time historical backfill of per-day user-analytics deltas.
-- Aggregates all existing study_history into user_daily_analytics, one row per
-- user/role/day holding that day's activity only (the same per-day delta grain
-- the nightly UserDailyAnalyticsJob produces). Only fully completed days are
-- filled (start_time < current_date); the current day is left to the nightly job
-- once it ends. The unique (user_id, role_name, day) constraint is respected
-- because each (day, user_id, role_name) combination yields exactly one group.

insert into user_daily_analytics (day, user_id, role_name, first_done, last_done,
                                  spent_time, done_exercises)
select cast(s.start_time as date),
       s.user_id,
       r.name,
       min(s.start_time),
       max(s.start_time),
       coalesce(sum(s.spent_time_in_seconds), 0),
       count(distinct s.exercise_id)
from study_history s,
     user_roles ur,
     role r
where s.user_id = ur.user_id
  and ur.role_id = r.id
  and s.start_time < current_date
group by cast(s.start_time as date), s.user_id, r.name;
