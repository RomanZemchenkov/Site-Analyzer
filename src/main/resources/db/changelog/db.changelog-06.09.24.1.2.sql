--liquibase formatted sql

--changeset roman:1
ALTER TABLE lemma
ADD CONSTRAINT unique_lemma_and_site_idx UNIQUE (site_id, lemma);

--changeset roman:2
CREATE INDEX idx_lemma_and_site_id ON lemma(lemma,site_id);
