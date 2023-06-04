alter table endpoint
    drop foreign key FKem31x69jtd2uwvrd9ityg7nd3;
alter table endpoint
    drop column status_id;

drop table if exists error;
drop table if exists warning;
drop table if exists information;
drop table if exists `status`;

create table if not exists error
(
    id            bigint auto_increment primary key,
    version       datetime(6)  null,
    message       varchar(255) null,
    message_id    varchar(255) null,
    response_code int          null,
    response_type varchar(255) null,
    timestamp     bigint       null,
    errors_id     bigint       null,
    constraint FKsqyofw1id0nnlsfkilojoktf
        foreign key (errors_id) references endpoint (id)
);

create table if not exists warning
(
    id            bigint auto_increment primary key,
    version       datetime(6)  null,
    message       varchar(255) null,
    message_id    varchar(255) null,
    response_code int          null,
    response_type varchar(255) null,
    timestamp     bigint       null,
    warnings_id   bigint       null,
    constraint FKbtlnllqhe5j5k631ujad8rvgm
        foreign key (warnings_id) references endpoint (id)
);

create table if not exists information
(
    id             bigint auto_increment primary key,
    version        datetime(6)  null,
    message        varchar(255) null,
    message_id     varchar(255) null,
    response_code  int          null,
    response_type  varchar(255) null,
    timestamp      bigint       null,
    information_id bigint       null,
    constraint voxuz8ppg813jl7g0za7
        foreign key (information_id) references endpoint (id)
);
