CREATE INDEX IF NOT EXISTS idx_contact_contributor_id
    ON contact (contributor_id);

CREATE INDEX IF NOT EXISTS idx_contributor_type_active_contribution
    ON contributor (type, active, contribution DESC);
