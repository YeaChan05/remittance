create schema if not exists core;
create schema if not exists integration;

drop table if exists core.account;
drop table if exists core.ledger;
drop table if exists core.member;
drop table if exists core.transfer;
drop table if exists integration.idempotency_key;
drop table if exists integration.outbox_events;
drop table if exists integration.processed_events;
create table core.account
(
    balance        decimal(38, 2) not null,
    created_at     datetime(6),
    id             bigint         not null,
    member_id      bigint         not null,
    updated_at     datetime(6),
    account_name   varchar(255)   not null,
    account_number varchar(255)   not null,
    bank_code      varchar(255)   not null,
    primary key (id)
) engine = InnoDB;
create table core.ledger
(
    amount      decimal(38, 2)          not null,
    account_id  bigint                  not null,
    created_at  datetime(6),
    id          bigint                  not null,
    transfer_id bigint                  not null,
    updated_at  datetime(6),
    side        enum ('CREDIT','DEBIT') not null,
    primary key (id)
) engine = InnoDB;
create table core.member
(
    created_at    datetime(6),
    id            bigint       not null,
    updated_at    datetime(6),
    email         varchar(255) not null,
    name          varchar(255) not null,
    password_hash varchar(255) not null,
    primary key (id)
) engine = InnoDB;
create table core.transfer
(
    amount          decimal(38, 2)                            not null,
    completed_at    datetime(6),
    created_at      datetime(6),
    from_account_id bigint                                    not null,
    id              bigint                                    not null,
    requested_at    datetime(6)                               not null,
    to_account_id   bigint                                    not null,
    updated_at      datetime(6),
    scope           enum ('DEPOSIT','REFUND','WITHDRAW','TRANSFER') not null,
    status          enum ('FAILED','IN_PROGRESS','SUCCEEDED') not null,
    primary key (id)
) engine = InnoDB;
alter table core.ledger
    add constraint uk_ledger_transfer_account_side unique (transfer_id, account_id, side);
alter table core.member
    add constraint UKmbmcqelty0fbrvxp1q58dn57t unique (email);
create table integration.idempotency_key
(
    client_id         bigint            not null,
    completed_at      datetime(6),
    created_at        datetime(6),
    expires_at        datetime(6)       not null,
    id                bigint            not null,
    started_at        datetime(6),
    updated_at        datetime(6),
    idempotency_key   varchar(255)      not null,
    request_hash      varchar(255),
    response_snapshot varchar(255),
    scope             enum ('TRANSFER') not null,
    status            enum ('BEFORE_START','FAILED','IN_PROGRESS','SUCCEEDED','TIMEOUT'),
    primary key (id)
) engine = InnoDB;
create table integration.outbox_events
(
    created_at     datetime(6),
    id             bigint              not null,
    updated_at     datetime(6),
    aggregate_id   varchar(255)        not null,
    aggregate_type varchar(255)        not null,
    event_type     varchar(255)        not null,
    payload        varchar(255)        not null,
    status         enum ('NEW','SENT') not null,
    primary key (id)
) engine = InnoDB;
create table integration.processed_events
(
    created_at   datetime(6),
    event_id     bigint      not null,
    id           bigint      not null,
    processed_at datetime(6) not null,
    updated_at   datetime(6),
    primary key (id)
) engine = InnoDB;
alter table integration.idempotency_key
    add constraint uk_idempotency_key_client_scope unique (client_id, scope, idempotency_key);
alter table integration.processed_events
    add constraint UKeh7hckmkmtw52e4abs8sorqby unique (event_id);
