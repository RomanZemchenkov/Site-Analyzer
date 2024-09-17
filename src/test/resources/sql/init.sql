DROP TABLE IF EXISTS index;
DROP TABLE IF EXISTS lemma;
DROP TABLE IF EXISTS page;
DROP TABLE IF EXISTS statistic;
DROP TABLE IF EXISTS site;

CREATE TABLE site
(
    id SERIAL PRIMARY KEY,
    status VARCHAR(32) NOT NULL,
    status_time TIMESTAMP WITH TIME ZONE NOT NULL,
    last_error TEXT,
    url VARCHAR(256) NOT NULL,
    name VARCHAR(256) NOT NULL
);

CREATE TABLE page
(
    id SERIAL PRIMARY KEY,
    site_id INTEGER REFERENCES site(id)NOT NULL,
    path TEXT NOT NULL,
    code INTEGER NOT NULL,
    content TEXT NOT NULL
);

CREATE TABLE lemma
(
    id SERIAL PRIMARY KEY,
    site_id INTEGER NOT NULL REFERENCES site(id),
    lemma VARCHAR(256) NOT NULL,
    frequency INTEGER NOT NULL
);

CREATE INDEX idx_site_id ON lemma(site_id);
-- CREATE INDEX idx_lemma_and_site_id ON lemma(lemma,site_id);

CREATE TABLE index
(
    id SERIAL PRIMARY KEY,
    page_id INTEGER NOT NULL REFERENCES page(id),
    lemma_id INTEGER NOT NULL REFERENCES lemma(id),
    rank FLOAT NOT NULL
);

CREATE INDEX idx_page_id ON index(page_id);

CREATE TABLE statistic
(
    id BIGINT PRIMARY KEY REFERENCES site(id),
    pages BIGINT,
    lemmas BIGINT
);