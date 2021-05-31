alter table user_account add column if not exists description varchar;
alter table user_account add column if not exists photo varchar;
alter table user_account
        add column if not exists doctor bigint,
        add constraint user_account_doctor_constraint
        foreign key (doctor)
        references user_account(id);
