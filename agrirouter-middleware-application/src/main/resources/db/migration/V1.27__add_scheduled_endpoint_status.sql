create table if not exists connection_state
(
    id        bigint auto_increment primary key,
    version   datetime(6) not null,
    cached    boolean,
    expired   boolean,
    expiry    datetime(6) not null,
    connected boolean
);

create table if not exists endpoint_status
(
    id                              bigint auto_increment primary key,
    version                         datetime(6) not null,
    nr_of_messages_within_the_inbox integer     not null,
    connection_state_id             bigint      null,
    constraint 5k5wnhpp84wzjv4
        foreign key (connection_state_id) references connection_state (id)
);

alter table endpoint
    add column endpoint_status_id bigint null;
alter table endpoint
    add constraint vg9mdxd1cnn0 foreign key (endpoint_status_id) references endpoint_status (id);
