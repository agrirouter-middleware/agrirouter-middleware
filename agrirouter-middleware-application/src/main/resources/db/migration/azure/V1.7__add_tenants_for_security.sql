create table if not exists tenant
(
    id           bigint auto_increment primary key,
    version      datetime(6)  not null,
    tenant_id    varchar(255) not null unique,
    name         varchar(255) not null unique,
    access_token varchar(255) not null
);
