alter table endpoint
    add column disabled boolean;

# noinspection SqlWithoutWhere

update endpoint
set endpoint.disabled = false;