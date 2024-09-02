--liquibase formatted sql

--changeset roman:1
CREATE TABLE site
(
    id SERIAL PRIMARY KEY,
    status VARCHAR(32) NOT NULL,
    status_time TIMESTAMP WITH TIME ZONE NOT NULL,
    last_error TEXT,
    url VARCHAR(256) NOT NULL,
    name VARCHAR(256) NOT NULL
);

--changeset roman:2
CREATE TABLE page
(
    id SERIAL PRIMARY KEY,
    site_id INTEGER REFERENCES site(id) ON DELETE CASCADE NOT NULL,
    path TEXT NOT NULL,
    code INTEGER NOT NULL,
    content TEXT NOT NULL
);

--changeset roman:3
CREATE INDEX unique_path_idx ON page (path)