CREATE TABLE IF NOT EXISTS user_schema.role_permissions
(
    role_id       UUID NOT NULL,
    permission_id UUID NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role
        FOREIGN KEY (role_id)
            REFERENCES user_schema.roles (role_id)
            ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission
        FOREIGN KEY (permission_id)
            REFERENCES user_schema.permissions (permission_id)
            ON DELETE CASCADE
);