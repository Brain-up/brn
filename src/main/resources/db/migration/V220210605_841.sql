alter table user_account add column if not exists description varchar;
alter table user_account add column if not exists photo varchar;
alter table user_account
        add column if not exists doctor_id bigint,
        add constraint user_account_doctor_constraint
        foreign key (doctor_id)
        references user_account(id);

alter table resource alter column description TYPE varchar;