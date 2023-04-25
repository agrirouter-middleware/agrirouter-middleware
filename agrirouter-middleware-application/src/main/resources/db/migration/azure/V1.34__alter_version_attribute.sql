alter table application
    drop column version;
alter table application_settings
    drop column version;
alter table authentication
    drop column version;
alter table business_log_event
    drop column version;
alter table connection_criteria
    drop column version;
alter table connection_state
    drop column version;
alter table content_message
    drop column version;
alter table content_message_metadata
    drop column version;
alter table ddi_combination_to_subscribe_for
    drop column version;
alter table endpoint
    drop column version;
alter table endpoint_status
    drop column version;
alter table error
    drop column version;
alter table information
    drop column version;
alter table router_device
    drop column version;
alter table supported_technical_message_type
    drop column version;
alter table tenant
    drop column version;
alter table unprocessed_message
    drop column version;
alter table warning
    drop column version;

alter table application
    add column version int;
alter table application_settings
    add column version int;
alter table authentication
    add column version int;
alter table business_log_event
    add column version int;
alter table connection_criteria
    add column version int;
alter table connection_state
    add column version int;
alter table content_message
    add column version int;
alter table content_message_metadata
    add column version int;
alter table ddi_combination_to_subscribe_for
    add column version int;
alter table endpoint
    add column version int;
alter table endpoint_status
    add column version int;
alter table error
    add column version int;
alter table information
    add column version int;
alter table router_device
    add column version int;
alter table supported_technical_message_type
    add column version int;
alter table tenant
    add column version int;
alter table unprocessed_message
    add column version int;
alter table warning
    add column version int;

update  application
    set version = 1;
update  application_settings
    set version = 1;
update  authentication
    set version = 1;
update  business_log_event
    set version = 1;
update  connection_criteria
    set version = 1;
update  connection_state
    set version = 1;
update  content_message
    set version = 1;
update  content_message_metadata
    set version = 1;
update  ddi_combination_to_subscribe_for
    set version = 1;
update  endpoint
    set version = 1;
update  endpoint_status
    set version = 1;
update  error
    set version = 1;
update  information
    set version = 1;
update  router_device
    set version = 1;
update  supported_technical_message_type
    set version = 1;
update  tenant
    set version = 1;
update  unprocessed_message
    set version = 1;
update  warning
    set version = 1;

alter table application
    add column last_update datetime(6);
alter table application_settings
    add column last_update datetime(6);
alter table authentication
    add column last_update datetime(6);
alter table business_log_event
    add column last_update datetime(6);
alter table connection_criteria
    add column last_update datetime(6);
alter table connection_state
    add column last_update datetime(6);
alter table content_message
    add column last_update datetime(6);
alter table content_message_metadata
    add column last_update datetime(6);
alter table ddi_combination_to_subscribe_for
    add column last_update datetime(6);
alter table endpoint
    add column last_update datetime(6);
alter table endpoint_status
    add column last_update datetime(6);
alter table error
    add column last_update datetime(6);
alter table information
    add column last_update datetime(6);
alter table router_device
    add column last_update datetime(6);
alter table supported_technical_message_type
    add column last_update datetime(6);
alter table tenant
    add column last_update datetime(6);
alter table unprocessed_message
    add column last_update datetime(6);
alter table warning
    add column last_update datetime(6);