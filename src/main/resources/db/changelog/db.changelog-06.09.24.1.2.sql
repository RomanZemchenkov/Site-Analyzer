--liquibase formatted sql

--changeset roman:1
ALTER TABLE lemma
ADD CONSTRAINT unique_lemma_and_site_idx UNIQUE (site_id, lemma);
