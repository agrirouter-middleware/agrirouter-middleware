drop table if exists device;

create table if not exists device
(
    id                                bigint auto_increment primary key,
    version                           datetime(6)  not null,
    base64_encoded_device_description longtext     not null,
    team_set_context_id               varchar(255) not null,
    endpoint_id                       bigint       not null,
    constraint yekgw2ltewzxqr3dd6du
        unique (team_set_context_id),
    constraint vfmmayh0y2xqf12rhmsi
        foreign key (endpoint_id) references endpoint (id)
)