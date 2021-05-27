alter table user_account
    add column changed_by varchar(255) not null default 'InitialDataLoader';