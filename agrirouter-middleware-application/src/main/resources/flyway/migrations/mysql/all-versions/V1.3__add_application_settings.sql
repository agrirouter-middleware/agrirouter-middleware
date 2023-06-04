create table if not exists application_settings
(
    id             bigint auto_increment primary key,
    version        datetime(6) null,
    application_id varchar(36) not null
);

create table if not exists ddi_combination_to_subscribe_for
(
    id                      bigint auto_increment primary key,
    version                 datetime(6) null,
    start                   integer,
    end                     integer,
    application_settings_id bigint
);

alter table ddi_combination_to_subscribe_for
    add foreign key z22l8WQs2rNK1M4gxj8J (application_settings_id) references application_settings (id);

alter table application
    add column application_settings_id bigint;

alter table application
    add foreign key Q2Cqn6KurQNHbuNKVKes (application_settings_id) references application_settings (id);

