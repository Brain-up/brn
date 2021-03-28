alter table resource add column if not exists locale VARCHAR (255);
update resource set locale = 'ru-ru' where id > 0;