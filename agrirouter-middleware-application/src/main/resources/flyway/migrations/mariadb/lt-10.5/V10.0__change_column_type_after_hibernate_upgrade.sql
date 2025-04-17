alter table authentication
    add column new_certificate text;

update authentication
set new_certificate = certificate
where authentication.certificate != '';

alter table authentication drop column certificate;

alter table authentication change new_certificate certificate clob;