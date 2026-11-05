-- #2609 Finalize the user_daily_analytics shape and recompute history.
--
-- The table was shipped in V220261008_2609.sql and backfilled in V220261011_2609.sql; both are
-- already applied and immutable (Flyway checksums), so extend and correct them via this new migration.
--
-- Documents the time units and adds two columns needed before any read path can be served from the
-- store:
--   * spent_time            -- that day's wall-clock study time in SECONDS (end-start), used by
--                              /admin/users (comment only; column already exists);
--   * unique_done_exercises -- distinct exercises that day;
--   * execution_time        -- that day's active execution time in SECONDS (sum of
--                              study_history.execution_seconds), the field the week/month/year graphs sum.
-- And changes done_exercises to count ALL completions (repeats included), not distinct exercises:
-- the product counts "how much the user trained", so 10 completions of one exercise count as 10.
--
-- Drops role_name from the grain. A study_history row has no role; role is a (possibly multi-valued)
-- property of the user, so a per-(user,role,day) key duplicated each day's figures per role and
-- double-counted multi-role users. The grain becomes (user_id, day); a reader that needs a role cohort
-- filters the user set by role and aggregates by user_id. This also lets users with no role be counted.
--
-- The earlier backfill used the old distinct semantics and the old grain, so recompute every
-- already-finalized day (day < current_date) from study_history with the corrected formula. The current
-- day is left to the incremental path + nightly reconciler. Every statement is idempotent so the
-- characterization IT can re-run the whole script after Flyway has already applied it at startup.

comment on column user_daily_analytics.spent_time is 'seconds';

alter table user_daily_analytics add column if not exists unique_done_exercises integer;
alter table user_daily_analytics add column if not exists execution_time bigint;
comment on column user_daily_analytics.execution_time is 'seconds';

alter table user_daily_analytics drop constraint if exists user_daily_analytics_uq_user_role_day;
drop index if exists user_daily_analytics_ix_role_name;
alter table user_daily_analytics drop column if exists role_name;

do $$
begin
    if not exists (select 1 from pg_constraint where conname = 'user_daily_analytics_uq_user_day') then
        alter table user_daily_analytics add constraint user_daily_analytics_uq_user_day unique (user_id, day);
    end if;
end $$;

delete from user_daily_analytics where day < current_date;

insert into user_daily_analytics (day, user_id, first_done, last_done,
                                  spent_time, execution_time, done_exercises, unique_done_exercises)
select cast(s.start_time as date),
       s.user_id,
       min(s.start_time),
       max(s.start_time),
       coalesce(sum(s.spent_time_in_seconds), 0),
       coalesce(sum(s.execution_seconds), 0),
       count(*),
       count(distinct s.exercise_id)
from study_history s
where s.start_time < current_date
group by cast(s.start_time as date), s.user_id;
