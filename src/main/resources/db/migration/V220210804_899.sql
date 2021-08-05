create table sin_audiometry_result
(
    id  bigint generated by default as identity
        constraint sin_audiometry_result_pkey
            primary key,
    frequency integer,
    sound_level integer,
    audiometry_history_id     bigint
        constraint audiometry_history_id_constraint
            references audiometry_history
);