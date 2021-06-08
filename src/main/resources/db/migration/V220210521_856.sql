ALTER TABLE exercise_group ADD COLUMN code varchar(255);

UPDATE exercise_group set code = 'NON_SPEECH_RU_RU' where locale = 'ru-ru' and name = 'Неречевые упражнения';
UPDATE exercise_group set code = 'SPEECH_RU_RU' where locale = 'ru-ru' and name = 'Речевые упражнения';
UPDATE exercise_group set code = 'NON_SPEECH_EN_US' where locale = 'en-us' and name = 'Non-Speech exercises';
UPDATE exercise_group set code = 'SPEECH_EN_US' where locale = 'en-us' and name = 'Speech exercises';

ALTER TABLE exercise_group ALTER COLUMN code SET NOT NULL;