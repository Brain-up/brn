drop index if exists audio_file_idx;

drop index if exists word_audio_file_idx;

create index if not exists word_word_type_idx on resource (word, word_type);

alter table resource drop constraint if exists resource_constrain;

alter table resource drop constraint if exists ukt37wptru4xcs8es5ae1huynph;

drop index if exists ukt37wptru4xcs8es5ae1huynph;

CREATE TABLE temp_resource_mapping (
    old_id bigint,
    new_id bigint
);

WITH duplicates AS (
    SELECT
        word,
        word_type,
        array_agg(id) AS ids,
        min(id) AS keep_id
    FROM
        resource
    GROUP BY
        word, word_type
    HAVING
        count(*) > 1
) INSERT INTO temp_resource_mapping (old_id, new_id)
  SELECT
    r.id,
    d.keep_id
  FROM
    resource r
  JOIN
    duplicates d ON r.word = d.word AND
                   (r.word_type = d.word_type OR (r.word_type IS NULL AND d.word_type IS NULL))
  WHERE
    r.id != d.keep_id AND
    r.id = ANY(d.ids);

UPDATE task
SET resource_id = tm.new_id
FROM temp_resource_mapping tm
WHERE task.resource_id = tm.old_id;

UPDATE task_resources
SET resource_id = tm.new_id
FROM temp_resource_mapping tm
WHERE task_resources.resource_id = tm.old_id;

DELETE FROM resource
WHERE id IN (SELECT old_id FROM temp_resource_mapping);

DROP TABLE temp_resource_mapping;

alter table resource add constraint resource_constrain unique (word, word_type);

alter table resource drop column audio_file_url;
