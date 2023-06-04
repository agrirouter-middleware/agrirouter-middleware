alter table application
    add private_key longtext;

alter table application
    add application_type varchar(255);

alter table application
    add public_key longtext;