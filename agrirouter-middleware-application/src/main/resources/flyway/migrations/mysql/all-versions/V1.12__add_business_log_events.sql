create table if not exists business_log_event
(
    id                      bigint auto_increment primary key,
    version                 datetime(6)  not null,
    application_id          bigint,
    endpoint_id             bigint,
    business_log_event_type varchar(50)  not null,
    message                 varchar(255) not null
);

alter table business_log_event
    add constraint `649e1d35b065425`
        foreign key (application_id) references application (id);

alter table business_log_event
    add constraint `c8a87b473540892`
        foreign key (endpoint_id) references endpoint (id);