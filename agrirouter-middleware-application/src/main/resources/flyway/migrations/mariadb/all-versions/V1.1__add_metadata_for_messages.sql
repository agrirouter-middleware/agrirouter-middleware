create table if not exists content_message_metadata
(
    id                     bigint auto_increment primary key,
    version                datetime(6)  not null,
    message_id             varchar(255) not null,
    technical_message_type varchar(255) not null,
    timestamp              bigint,
    receiver_id            varchar(255) null,
    filename               varchar(255) null,
    chunk_context_id       varchar(255) null,
    current_chunk          bigint,
    total_chunks           bigint,
    total_chunk_size       bigint,
    payload_size           bigint,
    sender_id              varchar(255) null,
    sequence_number        bigint,
    team_set_context_id    varchar(255) null
);

alter table content_message
    add column content_message_metadata_id bigint null;

alter table content_message
    add constraint veg5md5nuxrcguh9
        foreign key (content_message_metadata_id) references content_message_metadata (id);

alter table error
    add column message_id varchar(255) not null;

alter table error
    add column response_type varchar(255) not null;

alter table error
    add column response_code int not null;

alter table error
    add column timestamp bigint not null;

alter table warning
    add column message_id varchar(255) not null;

alter table warning
    add column response_type varchar(255) not null;

alter table warning
    add column response_code int not null;

alter table warning
    add column timestamp bigint not null;

create table if not exists information
(
    id            bigint auto_increment primary key,
    version       datetime(6)  null,
    message       varchar(255) null,
    message_id    varchar(255) null,
    response_code int          null,
    response_type varchar(255) null,
    timestamp     bigint       null,
    information_id     bigint       null,
    constraint voxuz8ppg813jl7g0za7
        foreign key (information_id) references status (id)
);

