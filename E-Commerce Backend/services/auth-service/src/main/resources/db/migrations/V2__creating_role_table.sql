CREATE TABLE IF NOT EXISTS user_schema.roles
(
    role_id     UUID PRIMARY KEY,
    role_name   VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    CONSTRAINT uk_role_role_name UNIQUE (role_name)
);