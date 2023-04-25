# alter table error rename column errors_id to endpoint_id;
alter table error
    change column errors_id endpoint_id bigint not null;

# alter table warning rename column warnings_id to endpoint_id;
alter table warning
    change column warnings_id endpoint_id bigint not null;

# alter table information rename column information_id to endpoint_id;
alter table information
    change column information_id endpoint_id bigint not null;