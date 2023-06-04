alter table application
    drop column if exists version;
alter table application_settings
    drop column if exists version;
alter table authentication
    drop column if exists version;
alter table connection_criteria
    drop column if exists version;
alter table connection_state
    drop column if exists version;
alter table content_message
    drop column if exists version;
alter table content_message_metadata
    drop column if exists version;
alter table ddi_combination_to_subscribe_for
    drop column if exists version;
alter table endpoint
    drop column if exists version;
alter table endpoint_status
    drop column if exists version;
alter table error
    drop column if exists version;
alter table message_recipient
    drop column if exists version;
alter table router_device
    drop column if exists version;
alter table supported_technical_message_type
    drop column if exists version;
alter table tenant
    drop column if exists version;
alter table unprocessed_message
    drop column if exists version;
alter table warning
    drop column if exists version;