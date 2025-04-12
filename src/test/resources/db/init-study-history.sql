INSERT INTO public.role (id, name) VALUES
(1, 'ADMIN'),
(2, 'USER');

INSERT INTO public.user_account (id, active, avatar, born_year, changed, changed_by, created, description, email, full_name, gender, password, photo, user_id, doctor_id, is_firebase_error, last_visit) VALUES
(1, true, '1', 1990, '2022-11-23 11:18:09.173318', 'admin@admin.com', '2021-07-30 08:13:15.587000', null, 'admin@admin.com', 'Elena Mosh', 'MALE', '$2a$10$0pmieR1fAW3IZO3xFbVjj.c9i2jhhGf/rFdLkG3A0lH89SpW1Mxdu', null, 'f89e5760-0caf-4a95-9810-cd6aa4a8261e', null, false, '2024-12-22 11:52:53.535561'),
(2, true, '13', 2024, '2024-03-29 16:51:09.788167', 'default@default.ru', '2021-07-30 08:13:15.673000', null, 'default@default.ru', 'AAA BBB', 'FEMALE', '$2a$10$P0oKm.pXyft/do/xMmR4f.q7a8MjTwCTrncOmM3khzuJoFOlhVtT6', null, '5cbe5936-9201-4f27-b148-273d6e1691b3', null, false, '2025-01-10 02:15:38.984504');
INSERT INTO public.user_roles (user_id, role_id) VALUES
(1, 1),
(1, 2),
(2, 2);

INSERT INTO public.exercise_group (id, code, description, locale, name) VALUES
(2, 'SPEECH_RU_RU', 'Речевые упражнения', 'ru-ru', 'Речевые упражнения (готовы для занятий)');

INSERT INTO public.series (id, description, level, name, type, exercise_group_id) VALUES
(1, 'Распознавание слов', 1, 'Слова', 'SINGLE_SIMPLE_WORDS', 2),
(17, 'Слова по методическому пособию Инны Васильевны Королевой Учусь слушать и говорить', 8, 'Слова Королёвой', 'SINGLE_WORDS_KOROLEVA', 2);

INSERT INTO public.sub_group (id, code, description, level, name, exercise_series_id, with_pictures) VALUES
(1, 'family', 'Слова про семью', 1, 'Семья', 1, false),
(115, 'koroleva_words_first_1', '1я группа слов: по одному', 1, '1 слово из 2..4 (1)', 17, false),
(25, 'music', 'Музыка', 25, 'Музыка', 1, false);

INSERT INTO public.exercise (id, active, changed_by, changed_when, level, name, noise_level, noise_url, template, sub_group_id, play_words_count, words_columns) VALUES
(1907, true, 'InitialDataLoader', '2022-01-23 19:33:18.747434', 3, '1я группа', 0, '', '', 115, 1, 2),
(1, true, 'InitialDataLoader', '2021-07-30 08:13:19.820000', 1, 'Семья', 0, '', '', 1, 1, 3),
(2, true, 'InitialDataLoader', '2021-07-30 08:13:19.884000', 2, 'Семья', 0, '', '', 1, 1, 3),
(689, true, 'InitialDataLoader', '2021-07-30 08:14:20.473000', 1, 'Музыка', 0, '', '', 25, 1, 3);

INSERT INTO public.study_history (id, end_time, execution_seconds, repetition_index, replays_count, right_answers_index, start_time, tasks_count, wrong_answers, exercise_id, user_id, spent_time_in_seconds) VALUES
(11723, now() - interval '1 week' + interval '1 hour', 12, 0, 0, 1, now() - interval '1 week' - interval '1 hour', 6, 0, 1907, 2, 15),
(4320, now() - interval '1 week' + interval '1 hour', 23, 0.1, 1, 1, now() - interval '1 week' - interval '1 hour', 9, 0, 1, 1, 27),
(4321, now() - interval '1 week' + interval '1 hour', 19, 0.1, 1, 1, now() - interval '1 week' - interval '1 hour', 9, 0, 2, 1, 23),
(8122, now() - interval '1 week' + interval '1 hour', 16, 0, 0, 1, now() - interval '1 week' - interval '1 hour', 9, 0, 689, 2, 21);