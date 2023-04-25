# Rename the former 'endpointId' to a more specific name.

alter table endpoint
    add column agrirouter_endpoint_id varchar(255);

# noinspection SqlWithoutWhere

update endpoint
set endpoint.agrirouter_endpoint_id = endpoint.endpoint_id;

# Remove former columns after update.

alter table endpoint
    drop column endpoint_id;

# Add an internal endpoint id column.

alter table endpoint
    add column internal_endpoint_id varchar(255);

#---------------------------------------------------------------------------------------------------------------------#

# Rename the former 'endpointId' to a more specific name.

alter table message_waiting_for_acknowledgement
    add column agrirouter_endpoint_id varchar(255);

# noinspection SqlWithoutWhere

update message_waiting_for_acknowledgement
set message_waiting_for_acknowledgement.agrirouter_endpoint_id = message_waiting_for_acknowledgement.endpoint_id;

# Remove former columns after update.

alter table message_waiting_for_acknowledgement
    drop column endpoint_id;

#---------------------------------------------------------------------------------------------------------------------#

# Rename the former 'endpointId' to a more specific name.

alter table content_message
    add column agrirouter_endpoint_id varchar(255);

# noinspection SqlWithoutWhere

update content_message
set content_message.agrirouter_endpoint_id = content_message.endpoint_id;

# Remove former columns after update.

alter table content_message
    drop column endpoint_id;

#---------------------------------------------------------------------------------------------------------------------#

# Rename the former 'endpointId' to a more specific name.

alter table unprocessed_message
    add column agrirouter_endpoint_id varchar(255);

# noinspection SqlWithoutWhere

update unprocessed_message
set unprocessed_message.agrirouter_endpoint_id = unprocessed_message.endpoint_id;

# Remove former columns after update.

alter table unprocessed_message
    drop column endpoint_id;

#---------------------------------------------------------------------------------------------------------------------#
# noinspection SqlWithoutWhere

update endpoint
set endpoint.internal_endpoint_id = concat('urn:agrirouter-middleware:', endpoint.agrirouter_endpoint_id)
where endpoint.endpoint_type = 'NON_VIRTUAL';

update endpoint
set endpoint.internal_endpoint_id = concat('urn:agrirouter-middleware:virtual:', endpoint.agrirouter_endpoint_id)
where endpoint.endpoint_type = 'VIRTUAL';

