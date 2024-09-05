--liquibase formatted sql

--changeset roman:1
CREATE TABLE lemma
(
    id SERIAL PRIMARY KEY,
    site_id INTEGER NOT NULL REFERENCES test.public.site(id) ON DELETE CASCADE,
    lemma VARCHAR(256) NOT NULL,
    frequency INTEGER NOT NULL
);

--changeset roman:2
CREATE TABLE index
(
    id SERIAL PRIMARY KEY,
    page_id INTEGER NOT NULL REFERENCES test.public.page(id) ON DELETE CASCADE,
    lemma_id INTEGER NOT NULL REFERENCES lemma(id) ON DELETE CASCADE,
    rank FLOAT NOT NULL
);