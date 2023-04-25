create table if not exists application
(
    id             bigint auto_increment primary key,
    version        datetime(6)  null,
    application_id varchar(36)  not null,
    name           varchar(255) not null,
    version_id     varchar(36)  not null,
    constraint UK_5ri2b0mk4909ck7a1rqw1fx56
        unique (version_id)
);

create table if not exists content_message
(
    id                     bigint auto_increment primary key,
    version                datetime(6)  null,
    endpoint_id            varchar(255) null,
    message                longtext     null,
    technical_message_type varchar(255) null
);

create table if not exists message_waiting_for_acknowledgement
(
    id                     bigint auto_increment primary key,
    version                datetime(6)  null,
    endpoint_id            varchar(255) null,
    message_id             varchar(255) null,
    response               varchar(255) null,
    technical_message_type varchar(255) null
);

create table if not exists status
(
    id      bigint auto_increment primary key,
    version datetime(6)  null,
    state   varchar(255) null
);

create table if not exists endpoint
(
    id                   bigint auto_increment primary key,
    version              datetime(6)  null,
    endpoint_id          varchar(255) not null,
    json                 longtext     null,
    status_id            bigint       null,
    onboard_responses_id bigint       null,
    constraint UK_dvcip7vh14xtb0cwgnlk62qhl
        unique (endpoint_id),
    constraint FKed6ghiu66k5itep5rbirq2sq3
        foreign key (onboard_responses_id) references application (id),
    constraint FKem31x69jtd2uwvrd9ityg7nd3
        foreign key (status_id) references status (id)
);

create table if not exists error
(
    id        bigint auto_increment primary key,
    version   datetime(6)  null,
    message   varchar(255) null,
    errors_id bigint       null,
    constraint FKsqyofw1id0nnlsfkilojoktf
        foreign key (errors_id) references status (id)
);

create table if not exists supported_technical_message_type
(
    id                                   bigint auto_increment primary key,
    version                              datetime(6)  null,
    direction                            varchar(255) null,
    technical_message_type               varchar(255) null,
    supported_technical_message_types_id bigint       null,
    constraint FKjtfwmpqvyw072kiv10wenrn7c
        foreign key (supported_technical_message_types_id) references application (id)
);

create table if not exists unprocessed_message
(
    id          bigint auto_increment primary key,
    version     datetime(6)  null,
    endpoint_id varchar(255) null,
    message     longtext     null
);

create table if not exists warning
(
    id          bigint auto_increment primary key,
    version     datetime(6)  null,
    message     varchar(255) null,
    warnings_id bigint       null,
    constraint FKbtlnllqhe5j5k631ujad8rvgm
        foreign key (warnings_id) references status (id)
);

