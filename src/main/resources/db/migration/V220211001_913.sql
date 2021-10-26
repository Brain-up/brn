alter table headphones add column if not exists active boolean;
update headphones
set active = true
where active isnull;