alter table if exists headphones
    add constraint unique_headphones_name_and_user unique (name, user_id);