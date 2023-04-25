# noinspection SqlWithoutWhere

delete
from business_log_event;

alter table business_log_event
    drop column if exists message;

alter table business_log_event
    add column message longtext;
