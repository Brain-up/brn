alter table user_account add column if not exists description varchar(255);
alter table user_account add column if not exists photo varchar(255);
alter table user_account
        add column if not exists doctor bigint,
        add constraint a47569ad8b6996ef5d8eea2e73d1e2ab
        foreign key (doctor)
        references user_account(id);
