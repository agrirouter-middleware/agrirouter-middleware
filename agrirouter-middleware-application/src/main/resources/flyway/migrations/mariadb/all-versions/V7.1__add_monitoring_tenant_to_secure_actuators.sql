alter table tenant
    add column monitoring_access boolean default false;

# noinspection SqlWithoutWhere
update tenant
set tenant.monitoring_access = false;

alter table tenant
    add column default_tenant boolean default false;

# noinspection SqlWithoutWhere
update tenant
set tenant.default_tenant = true
where tenant.generated_tenant = true;