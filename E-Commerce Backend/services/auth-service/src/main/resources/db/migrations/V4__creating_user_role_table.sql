CREATE TABLE IF NOT EXISTS user_schema.user_roles
(
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id)
            REFERENCES user_schema.users (user_id)
            ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id)
            REFERENCES user_schema.roles (role_id)
            ON DELETE CASCADE
);