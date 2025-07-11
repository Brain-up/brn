drop index if exists audio_file_idx;

drop index if exists word_audio_file_idx;

create index word_audio_file_idx on resource (word, wordType);

alter table resource drop constraint resource_constrain;

alter table resource add constraint resource_constrain unique (word, wordType);

alter table resource drop column audio_file_url;
