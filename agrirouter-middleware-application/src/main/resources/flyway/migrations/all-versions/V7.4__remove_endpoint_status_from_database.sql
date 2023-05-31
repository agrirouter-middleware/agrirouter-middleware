# Drop the existing FK constraint
alter table endpoint
    drop constraint vg9mdxd1cnn0;

# Drop the existing column
alter table endpoint
    drop column endpoint_status_id;

# Drop the existing table
drop table endpoint_status;