-- #2609 user_daily_analytics: add execution_time / unique_done_exercises, done_exercises = all
-- completions, drop role_name (grain is now user_id, day), recompute past days from study_history.
-- Idempotent: the IT re-runs this script.

-- Clear past days first: multi-role users' per-role rows would collide on (user_id, day) once role_name is dropped.
delete from user_daily_analytics where day < current_date;

comment on column user_daily_analytics.spent_time is 'seconds';

alter table user_daily_analytics add column if not exists unique_done_exercises integer;
alter table user_daily_analytics add column if not exists execution_time bigint;
comment on column user_daily_analytics.execution_time is 'seconds';

alter table user_daily_analytics drop constraint if exists user_daily_analytics_uq_user_role_day;
drop index if exists user_daily_analytics_ix_role_name;
alter table user_daily_analytics drop column if exists role_name;

alter table user_daily_analytics drop constraint if exists user_daily_analytics_uq_user_day;
alter table user_daily_analytics add constraint user_daily_analytics_uq_user_day unique (user_id, day);

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
