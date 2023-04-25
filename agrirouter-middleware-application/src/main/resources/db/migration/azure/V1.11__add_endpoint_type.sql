alter table endpoint
    add column endpoint_type VARCHAR(25) NOT NULL;

# noinspection SqlWithoutWhere

update endpoint
set endpoint.endpoint_type = 'NON_VIRTUAL';