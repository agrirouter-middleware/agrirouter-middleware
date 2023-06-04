alter table endpoint
    add constraint unique_external_endpoint_id
        unique (external_endpoint_id);