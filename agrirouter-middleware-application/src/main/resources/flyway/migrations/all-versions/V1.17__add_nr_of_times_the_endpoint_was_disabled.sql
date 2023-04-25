alter table endpoint
    add column nr_of_times_the_endpoint_was_disabled int;

# noinspection SqlWithoutWhere

update endpoint
set endpoint.nr_of_times_the_endpoint_was_disabled = 0;
