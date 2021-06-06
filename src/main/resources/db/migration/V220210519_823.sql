alter table exercise
    add column active boolean not null default true;
alter table exercise
    add column changed_when timestamp not null default current_timestamp;
alter table exercise
    add column changed_by varchar(255) not null default 'InitialDataLoader';