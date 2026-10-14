-- #2609 Finalize the user_daily_analytics shape and recompute history.
--
-- The table was shipped in V220261008_2609.sql and backfilled in V220261011_2609.sql; both are
-- already applied and immutable (Flyway checksums), so extend and correct them via this new migration.
--
-- Documents the time units and adds two columns needed before any read path can be served from the
-- store:
--   * spent_time           -- that day's wall-clock study time in SECONDS (end-start), used by
--                             /admin/users (comment only; column already exists);
--   * unique_done_exercises -- distinct exercises that day (what /admin/users' COUNT(DISTINCT) shows);
--   * execution_time        -- that day's active execution time in SECONDS (sum of
--                             study_history.execution_seconds), the field the week/month/year graphs sum.
-- And changes done_exercises to count ALL completions (repeats included), not distinct exercises:
-- the product counts "how much the user trained", so 10 completions of one exercise count as 10.
--
-- The earlier backfill used the old distinct semantics and lacked the new columns, so recompute every
-- already-finalized day (day < current_date) from study_history with the corrected formula. The current
-- day is left to the incremental path + nightly reconciler.

comment on column user_daily_analytics.spent_time is 'seconds';

alter table user_daily_analytics add column if not exists unique_done_exercises integer;
alter table user_daily_analytics add column if not exists execution_time bigint;
comment on column user_daily_analytics.execution_time is 'seconds';

delete from user_daily_analytics where day < current_date;

insert into user_daily_analytics (day, user_id, role_name, first_done, last_done,
                                  spent_time, execution_time, done_exercises, unique_done_exercises)
select cast(s.start_time as date),
       s.user_id,
       r.name,
       min(s.start_time),
       max(s.start_time),
       coalesce(sum(s.spent_time_in_seconds), 0),
       coalesce(sum(s.execution_seconds), 0),
       count(*),
       count(distinct s.exercise_id)
from study_history s,
     user_roles ur,
     role r
where s.user_id = ur.user_id
  and ur.role_id = r.id
  and s.start_time < current_date
group by cast(s.start_time as date), s.user_id, r.name;
