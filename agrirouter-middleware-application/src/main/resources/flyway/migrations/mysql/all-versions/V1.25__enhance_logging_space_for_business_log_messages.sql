# noinspection SqlWithoutWhere

delete
from business_log_event;

alter table business_log_event
    drop column message;

alter table business_log_event
    add column message longtext;
