/** Make database table same as from ddl-auto update. Add script resolves issues to create tables from Flyway.*/
ALTER TABLE series ADD COLUMN IF NOT EXISTS level integer;
ALTER TABLE sub_group ADD COLUMN IF NOT EXISTS code varchar(255) not null;
ALTER TABLE audiometry_task ALTER COLUMN count DROP NOT NULL;
ALTER TABLE audiometry_task ALTER COLUMN level DROP NOT NULL;
ALTER TABLE audiometry_task ALTER COLUMN ear TYPE varchar(255);
ALTER TABLE exercise ALTER COLUMN level SET NOT NULL;
ALTER TABLE exercise DROP COLUMN IF EXISTS picture_url;
ALTER TABLE exercise_group ALTER COLUMN locale TYPE varchar(255);
ALTER TABLE resource ALTER COLUMN locale TYPE varchar(255);
ALTER TABLE series ALTER COLUMN type TYPE varchar(255);
ALTER TABLE series ALTER COLUMN type DROP NOT NULL;
ALTER TABLE sub_group DROP COLUMN IF EXISTS exercise_type;
ALTER TABLE sub_group ALTER COLUMN level SET NOT NULL;
ALTER TABLE sub_group DROP COLUMN IF EXISTS picture;
ALTER TABLE sub_group DROP COLUMN IF EXISTS  template;
ALTER TABLE user_account ALTER COLUMN born_year DROP DEFAULT;
ALTER TABLE user_account ALTER COLUMN born_year DROP NOT NULL;
ALTER TABLE user_account ALTER COLUMN changed DROP DEFAULT;
ALTER TABLE user_account ALTER COLUMN created DROP DEFAULT;
ALTER TABLE user_account ALTER COLUMN email SET NOT NULL;
ALTER TABLE user_account ALTER COLUMN gender TYPE varchar(255);
ALTER TABLE user_account ALTER COLUMN gender DROP DEFAULT;
ALTER TABLE user_account ALTER COLUMN gender DROP NOT NULL;
ALTER TABLE authority ALTER COLUMN id DROP IDENTITY IF EXISTS;
ALTER TABLE authority ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY;
ALTER TABLE user_account ALTER COLUMN id DROP IDENTITY IF EXISTS;
ALTER TABLE user_account ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY;
ALTER TABLE exercise_group ALTER COLUMN id DROP IDENTITY IF EXISTS;
ALTER TABLE exercise_group ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY;
ALTER TABLE series ALTER COLUMN id DROP IDENTITY IF EXISTS;
ALTER TABLE series ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY;
ALTER TABLE sub_group ALTER COLUMN id DROP IDENTITY IF EXISTS;
ALTER TABLE sub_group ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY;
ALTER TABLE resource ALTER COLUMN id DROP IDENTITY IF EXISTS;
ALTER TABLE resource ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY;
ALTER TABLE exercise ALTER COLUMN id DROP IDENTITY IF EXISTS;
ALTER TABLE exercise ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY;
ALTER TABLE task ALTER COLUMN id DROP IDENTITY IF EXISTS;
ALTER TABLE task ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY;
ALTER TABLE signal ALTER COLUMN id DROP IDENTITY IF EXISTS;
ALTER TABLE signal ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY;
ALTER TABLE study_history ALTER COLUMN id DROP IDENTITY IF EXISTS;
ALTER TABLE study_history ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY;
