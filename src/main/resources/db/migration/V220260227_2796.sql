CREATE INDEX IF NOT EXISTS idx_contact_contributor_id
    ON contact (contributor_id);

CREATE INDEX IF NOT EXISTS idx_contributor_type_active_contribution
    ON contributor (type, active, contribution DESC);

CREATE INDEX IF NOT EXISTS idx_exercise_sub_group_id
    ON exercise (sub_group_id);

CREATE INDEX IF NOT EXISTS idx_task_exercise_id
    ON task (exercise_id);

CREATE INDEX IF NOT EXISTS idx_signal_exercise_id
    ON signal (exercise_id);

CREATE INDEX IF NOT EXISTS idx_task_resources_task_id
    ON task_resources (task_id);

CREATE INDEX IF NOT EXISTS idx_study_history_user_start_time
    ON study_history (user_id, start_time);
