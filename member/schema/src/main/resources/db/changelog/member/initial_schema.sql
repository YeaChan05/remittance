CREATE DATABASE IF NOT EXISTS core;

CREATE TABLE IF NOT EXISTS core.member
(
    id            BIGINT       NOT NULL PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at    DATETIME(6)  NULL,
    updated_at    DATETIME(6)  NULL,
    UNIQUE KEY uk_member_email (email)
);
