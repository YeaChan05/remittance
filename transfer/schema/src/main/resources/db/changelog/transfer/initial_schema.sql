CREATE DATABASE IF NOT EXISTS core;
CREATE DATABASE IF NOT EXISTS integration;

CREATE TABLE IF NOT EXISTS core.transfer
(
    id             BIGINT       NOT NULL PRIMARY KEY,
    from_account_id BIGINT      NOT NULL,
    to_account_id  BIGINT       NOT NULL,
    amount         DECIMAL(19,2) NOT NULL,
    scope          VARCHAR(50)  NOT NULL,
    status         VARCHAR(50)  NOT NULL,
    requested_at   DATETIME(6)  NOT NULL,
    completed_at   DATETIME(6)  NULL,
    created_at     DATETIME(6)  NULL,
    updated_at     DATETIME(6)  NULL
);

CREATE TABLE IF NOT EXISTS core.ledger
(
    id           BIGINT       NOT NULL PRIMARY KEY,
    transfer_id  BIGINT       NOT NULL,
    account_id   BIGINT       NOT NULL,
    amount       DECIMAL(19,2) NOT NULL,
    side         VARCHAR(50)  NOT NULL,
    created_at   DATETIME(6)  NULL,
    updated_at   DATETIME(6)  NULL,
    UNIQUE KEY uk_ledger_transfer_account_side (transfer_id, account_id, side)
);

CREATE TABLE IF NOT EXISTS core.daily_limit_usage
(
    id          BIGINT       NOT NULL PRIMARY KEY,
    account_id  BIGINT       NOT NULL,
    scope       VARCHAR(50)  NOT NULL,
    usage_date  DATE         NOT NULL,
    used_amount DECIMAL(19,2) NOT NULL,
    created_at  DATETIME(6)  NULL,
    updated_at  DATETIME(6)  NULL,
    UNIQUE KEY uk_daily_limit_usage_account_scope_date (account_id, scope, usage_date)
);

CREATE TABLE IF NOT EXISTS integration.idempotency_key
(
    id               BIGINT       NOT NULL PRIMARY KEY,
    client_id        BIGINT       NOT NULL,
    idempotency_key  VARCHAR(255) NOT NULL,
    expires_at       DATETIME(6)  NOT NULL,
    scope            VARCHAR(50)  NOT NULL,
    status           VARCHAR(50)  NULL,
    request_hash     VARCHAR(255) NULL,
    response_snapshot VARCHAR(255) NULL,
    started_at       DATETIME(6)  NULL,
    completed_at     DATETIME(6)  NULL,
    created_at       DATETIME(6)  NULL,
    updated_at       DATETIME(6)  NULL,
    UNIQUE KEY uk_idempotency_key_client_scope (client_id, scope, idempotency_key)
);

CREATE TABLE IF NOT EXISTS integration.outbox_events
(
    id             BIGINT       NOT NULL PRIMARY KEY,
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id   VARCHAR(255) NOT NULL,
    event_type     VARCHAR(255) NOT NULL,
    payload        VARCHAR(255) NOT NULL,
    status         VARCHAR(50)  NOT NULL,
    created_at     DATETIME(6)  NULL,
    updated_at     DATETIME(6)  NULL
);
