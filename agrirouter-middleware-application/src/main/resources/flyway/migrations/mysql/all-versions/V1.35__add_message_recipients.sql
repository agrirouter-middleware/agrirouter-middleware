drop table if exists message_recipient;

create table if not exists message_recipient
(
    id                     bigint auto_increment primary key,
    version                int          not null,
    last_update            datetime(6),
    agrirouter_endpoint_id varchar(255) not null,
    endpoint_name          varchar(255) not null,
    endpoint_type          varchar(50)  not null,
    external_id            varchar(255) not null,
    technical_message_type varchar(50)  not null,
    direction              varchar(20)  not null,
    endpoint_id            bigint
);

alter table message_recipient
    add constraint iyuklyiuxltvpnbs foreign key (endpoint_id) references endpoint (id);