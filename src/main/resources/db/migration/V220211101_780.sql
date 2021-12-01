alter table user_account add column if not exists is_firebase_error boolean;

update user_account set is_firebase_error = false;

CREATE INDEX user_account_id_is_firebase_error_idx ON user_account(id, is_firebase_error);