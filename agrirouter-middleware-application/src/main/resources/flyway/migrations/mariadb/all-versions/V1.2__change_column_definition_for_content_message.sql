alter table content_message
    drop if exists message;

alter table content_message
    add column message_content longblob;

