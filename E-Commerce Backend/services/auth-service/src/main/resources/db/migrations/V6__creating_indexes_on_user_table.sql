CREATE INDEX IF NOT EXISTS idx_user_username
    ON user_schema.users (username);

CREATE INDEX IF NOT EXISTS idx_user_email
    ON user_schema.users (email);