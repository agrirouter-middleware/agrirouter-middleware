alter table application
    add column tenant_id bigint;
alter table application
    add foreign key 6a3f71a9cd68 (tenant_id) references tenant (id);