ALTER TABLE audiometry_task ALTER COLUMN audiometry_group DROP NOT NULL;
ALTER TABLE audiometry_task ALTER COLUMN frequency_zone DROP NOT NULL;
ALTER TABLE audiometry_task ALTER COLUMN min_frequency DROP NOT NULL;
ALTER TABLE audiometry_task ALTER COLUMN max_frequency DROP NOT NULL;
ALTER TABLE audiometry_task ALTER COLUMN show_size DROP NOT NULL;

ALTER TABLE audiometry_task ADD COLUMN frequencies VARCHAR (255);
ALTER TABLE audiometry_task ADD COLUMN ear VARCHAR (10);