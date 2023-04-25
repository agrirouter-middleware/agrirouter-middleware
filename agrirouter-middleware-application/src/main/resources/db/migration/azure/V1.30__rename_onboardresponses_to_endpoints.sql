# alter table endpoint rename column onboard_responses_id to application_id;
alter table endpoint
    drop foreign key FKed6ghiu66k5itep5rbirq2sq3;
alter table endpoint
    change column onboard_responses_id application_id bigint not null;
alter table endpoint
    add foreign key FK4926e6f00491
         (application_id) references application (id);