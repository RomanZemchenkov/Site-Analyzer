--liquibase formatted sql

--changeset roman:1
CREATE INDEX page_site_id_idx ON page(site_id);