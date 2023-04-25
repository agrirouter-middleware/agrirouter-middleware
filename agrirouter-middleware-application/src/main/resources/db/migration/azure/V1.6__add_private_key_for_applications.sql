alter table application
    add column if not exists private_key longtext;

alter table application
    add column if not exists application_type varchar(255);

alter table application
    add column if not exists public_key longtext;