alter table content_message
    drop message;

alter table content_message
    add column message_content longblob;

