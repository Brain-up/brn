DROP INDEX audio_file_idx;
DROP INDEX word_audio_file_idx;
CREATE INDEX word_idx on resource (word, word_type);

ALTER TABLE resource DROP CONSTRAINT resource_constrain;
ALTER TABLE resource ADD CONSTRAINT resource_constrain unique (word, word_type);

ALTER TABLE resource DROP COLUMN audio_file_url;