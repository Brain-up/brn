drop index if exists audio_file_idx;

drop index if exists word_audio_file_idx;

create index if not exists word_word_type_idx on resource (word, word_type);

alter table resource drop constraint resource_constrain;

DELETE FROM resource
WHERE id NOT IN (
    SELECT MIN(id)
    FROM resource
    GROUP BY word, word_type
);

alter table resource add constraint resource_constrain unique (word, word_type);

alter table resource drop column audio_file_url;
