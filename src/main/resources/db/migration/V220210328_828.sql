alter table resource add column if not exists locale VARCHAR (20);
update resource set locale = 'ru-ru' where id > 0;
