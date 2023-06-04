# alter table endpoint rename column disabled to deactivated;
alter table endpoint
    change column disabled deactivated tinyint(1) default 0 not null;

# alter table endpoint rename column nr_of_times_the_endpoint_was_disabled to nr_of_times_the_endpoint_was_deactivated;
alter table endpoint
    change column nr_of_times_the_endpoint_was_disabled nr_of_times_the_endpoint_was_deactivated int(11) default 0 not null;