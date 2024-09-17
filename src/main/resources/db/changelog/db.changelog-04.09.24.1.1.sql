--liquibase formatted sql

--changeset roman:1
CREATE TABLE lemma
(
    id SERIAL PRIMARY KEY,
    site_id INTEGER NOT NULL REFERENCES site(id),
    lemma VARCHAR(256) NOT NULL,
    frequency INTEGER NOT NULL
);

--changeset roman:2
CREATE TABLE index
(
    id SERIAL PRIMARY KEY,
    page_id INTEGER NOT NULL REFERENCES page(id),
    lemma_id INTEGER NOT NULL REFERENCES lemma(id),
    rank FLOAT NOT NULL
);

--changeset roma:3
CREATE INDEX index_page_id_idx ON index(page_id);