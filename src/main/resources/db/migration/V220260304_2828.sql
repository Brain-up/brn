ALTER TABLE user_account
    ADD COLUMN IF NOT EXISTS auth_state_changed timestamp;

UPDATE user_account
SET auth_state_changed = COALESCE(auth_state_changed, changed, created, CURRENT_TIMESTAMP);

ALTER TABLE user_account
    ALTER COLUMN auth_state_changed SET DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE user_account
    ALTER COLUMN auth_state_changed SET NOT NULL;

CREATE OR REPLACE FUNCTION sync_user_account_auth_state_changed()
RETURNS trigger AS
$$
BEGIN
    IF NEW.email IS DISTINCT FROM OLD.email OR NEW.active IS DISTINCT FROM OLD.active THEN
        NEW.auth_state_changed = CURRENT_TIMESTAMP;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_user_account_auth_state_changed ON user_account;

CREATE TRIGGER trg_user_account_auth_state_changed
    BEFORE UPDATE
    ON user_account
    FOR EACH ROW
EXECUTE FUNCTION sync_user_account_auth_state_changed();

CREATE OR REPLACE FUNCTION sync_user_account_auth_state_changed_from_roles()
RETURNS trigger AS
$$
BEGIN
    IF TG_OP = 'DELETE' THEN
        UPDATE user_account
        SET auth_state_changed = CURRENT_TIMESTAMP
        WHERE id = OLD.user_id;
        RETURN OLD;
    END IF;

    UPDATE user_account
    SET auth_state_changed = CURRENT_TIMESTAMP
    WHERE id = NEW.user_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_user_roles_auth_state_changed ON user_roles;

CREATE TRIGGER trg_user_roles_auth_state_changed
    AFTER INSERT OR UPDATE OR DELETE
    ON user_roles
    FOR EACH ROW
EXECUTE FUNCTION sync_user_account_auth_state_changed_from_roles();
