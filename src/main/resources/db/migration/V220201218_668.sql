ALTER TABLE resource DROP CONSTRAINT uk7rqvk7iml0lvslr33ujqrbneu;
ALTER TABLE resource ADD CONSTRAINT resource_constrain unique (word, audio_file_url, word_type);

DROP INDEX word_audio_file_idx;
CREATE index word_audio_file_idx on resource (word, audio_file_url, word_type);