create table if not exists authentication
(
    id          bigint auto_increment primary key,
    version     datetime(6) null,
    type        varchar(25) not null,
    secret      varchar(50) not null,
    certificate longtext    not null
);

create table if not exists connection_criteria
(
    id        bigint auto_increment primary key,
    version   datetime(6)  null,
    client_id varchar(40)  not null,
    host      varchar(255) not null,
    port      varchar(10)  not null
);

create table if not exists router_device
(
    id                     bigint auto_increment primary key,
    version                datetime(6) null,
    device_alternate_id    varchar(40) not null,
    authentication_id      bigint      not null,
    connection_criteria_id bigint      not null,
    constraint b3f0078de7ca0e79
        foreign key (authentication_id) references authentication (id),
    constraint b9b3e1d9c047d7ee
        foreign key (connection_criteria_id) references connection_criteria (id)
);

alter table application_settings
    add column router_device_id bigint null;
alter table application_settings
    add constraint 9b3732a0123c6e0b
        foreign key (router_device_id) references router_device (id)
