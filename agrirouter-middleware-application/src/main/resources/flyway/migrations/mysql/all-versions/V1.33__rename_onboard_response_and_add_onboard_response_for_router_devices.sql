alter table endpoint rename column json to onboard_response;
alter table endpoint
    add column onboard_response_for_router_device longtext null;
alter table connection_state
    add column client_id varchar(50);