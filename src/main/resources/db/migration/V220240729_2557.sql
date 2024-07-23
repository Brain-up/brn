alter table user_roles
    drop constraint fk6teafluo1xt1re7vbgr16w8iy;

alter table user_roles
    add constraint user_roles_to_user_account
        foreign key (user_id) references user_account
            on delete cascade;

alter table study_history
    drop constraint fk5gkcspvt7bmyc80jv8vfbfpqt;

alter table study_history
    add constraint study_history_to_user_account
        foreign key (user_id) references user_account
            on delete cascade;			