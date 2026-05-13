CREATE TABLE IF NOT EXISTS user_schema.users
(
    user_id    UUID PRIMARY KEY,
    username   VARCHAR(100) NOT NULL,
    name       VARCHAR(150),
    email      VARCHAR(150) NOT NULL,
    password   VARCHAR(255),
    image      TEXT,
    oauth_id   VARCHAR(255),
    provider   VARCHAR(50)  NOT NULL DEFAULT 'LOCAL',
    enabled    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_username UNIQUE (username),
    CONSTRAINT uk_user_email UNIQUE (email)
);