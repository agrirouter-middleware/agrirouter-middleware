alter table endpoint
    add column parent_endpoint_id bigint;
alter table endpoint
    add constraint a9acd06ce7cf14b4a66b foreign key endpoint (parent_endpoint_id) references endpoint (id);