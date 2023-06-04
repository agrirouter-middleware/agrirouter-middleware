# Add an internal application id column.

alter table application
    add column internal_application_id varchar(255);

# noinspection SqlWithoutWhere

update application
set application.internal_application_id = concat('urn:agrirouter-middleware:application:', uuid());

update endpoint
set endpoint.internal_endpoint_id = concat('urn:agrirouter-middleware:endpoint:', uuid())
where endpoint.endpoint_type = 'NON_VIRTUAL';

update endpoint
set endpoint.internal_endpoint_id = concat('urn:agrirouter-middleware:endpoint:virtual:', uuid())
where endpoint.endpoint_type = 'VIRTUAL';
