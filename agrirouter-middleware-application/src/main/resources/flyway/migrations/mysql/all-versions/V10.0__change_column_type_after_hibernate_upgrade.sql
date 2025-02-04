alter table authentication
    add column new_certificate text;

update authentication
set new_certificate = certificate
where authentication.certificate != '';

alter table authentication
    drop column certificate;

alter table authentication rename column new_certificate to certificate;