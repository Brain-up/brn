alter table user_account rename column first_name to full_name;
alter table user_account drop column last_name;
alter table user_account drop column birthday;
alter table user_account add column born_year integer not null default 2000;
alter table user_account add column gender varchar(8) not null default 'MALE';
alter table user_account add check (gender in ('MALE', 'FEMALE'));