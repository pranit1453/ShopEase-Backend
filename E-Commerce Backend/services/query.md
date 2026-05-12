CREATE SCHEMA IF NOT EXISTS user_schema;

CREATE TABLE IF NOT EXISTS user_schema.users (

    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    username VARCHAR(100) NOT NULL,

    name VARCHAR(150),

    email VARCHAR(150) NOT NULL,

    password VARCHAR(255),

    image TEXT,

    oauth_id VARCHAR(255),

    provider VARCHAR(50) NOT NULL DEFAULT 'LOCAL',

    enabled BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_user_username UNIQUE(username),

    CONSTRAINT uk_user_email UNIQUE(email)

);

CREATE INDEX IF NOT EXISTS idx_user_username
ON user_schema.users(username);

CREATE INDEX IF NOT EXISTS idx_user_email
ON user_schema.users(email);

CREATE TABLE IF NOT EXISTS user_schema.roles (

    role_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    role_name VARCHAR(100) NOT NULL,

    description VARCHAR(255),

    CONSTRAINT uk_role_role_name UNIQUE(role_name)

);

CREATE INDEX IF NOT EXISTS idx_role_role_name
ON user_schema.roles(role_name);

CREATE TABLE IF NOT EXISTS user_schema.permissions (

    permission_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    permission_code VARCHAR(150) NOT NULL,

    module_name VARCHAR(100),

    action_name VARCHAR(100),

    description VARCHAR(255),

    CONSTRAINT uk_permission_code UNIQUE(permission_code)

);

CREATE INDEX IF NOT EXISTS idx_permission_code
ON user_schema.permissions(permission_code);

CREATE INDEX IF NOT EXISTS idx_permission_module
ON user_schema.permissions(module_name);

CREATE INDEX IF NOT EXISTS idx_permission_action
ON user_schema.permissions(action_name);

CREATE TABLE IF NOT EXISTS user_schema.user_roles (

    user_id UUID NOT NULL,

    role_id UUID NOT NULL,

    PRIMARY KEY(user_id, role_id),

    CONSTRAINT fk_user_roles_user
        FOREIGN KEY(user_id)
        REFERENCES user_schema.users(user_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_user_roles_role
        FOREIGN KEY(role_id)
        REFERENCES user_schema.roles(role_id)
        ON DELETE CASCADE

);

CREATE TABLE IF NOT EXISTS user_schema.role_permissions (

    role_id UUID NOT NULL,

    permission_id UUID NOT NULL,

    PRIMARY KEY(role_id, permission_id),

    CONSTRAINT fk_role_permissions_role
        FOREIGN KEY(role_id)
        REFERENCES user_schema.roles(role_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_role_permissions_permission
        FOREIGN KEY(permission_id)
        REFERENCES user_schema.permissions(permission_id)
        ON DELETE CASCADE

);

SELECT r.*, p.*
FROM roles r
LEFT JOIN role_permissions rp
ON r.role_id = rp.role_id
LEFT JOIN permissions p
ON rp.permission_id = p.permission_id
WHERE r.role_name = 'CUSTOMER';