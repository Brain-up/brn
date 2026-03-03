create index if not exists exercise_sub_group_id_idx
    on exercise (sub_group_id);

create index if not exists task_exercise_id_idx
    on task (exercise_id);

create index if not exists signal_exercise_id_idx
    on signal (exercise_id);
