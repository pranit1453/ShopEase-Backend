CREATE TABLE IF NOT EXISTS user_schema.permissions (
    permission_id UUID PRIMARY KEY,
    permission_code VARCHAR(150) NOT NULL,
    module_name VARCHAR(100),
    action_name VARCHAR(100),
    description VARCHAR(255),
    CONSTRAINT uk_permission_code UNIQUE(permission_code)
);