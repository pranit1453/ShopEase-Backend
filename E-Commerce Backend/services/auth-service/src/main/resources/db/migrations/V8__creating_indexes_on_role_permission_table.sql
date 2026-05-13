CREATE INDEX IF NOT EXISTS idx_permission_code
    ON user_schema.permissions (permission_code);

CREATE INDEX IF NOT EXISTS idx_permission_module
    ON user_schema.permissions (module_name);

CREATE INDEX IF NOT EXISTS idx_permission_action
    ON user_schema.permissions (action_name);