INSERT INTO site(status, status_time, url, name)
VALUES ('INDEXED', '2000-02-02 00:00:00.000+00', '/test/url', 'Test Site'),
       ('INDEXED', '2000-02-02 00:00:00.000+00', 'https://sendel.ru', 'Sendel.ru'),
       ('INDEXED', '2000-02-02 00:00:00.000+00', '/test1/url', 'Test Site1');


-- INSERT INTO page(site_id, path, code, content)
-- VALUES (1, '/path', 200, 'content'),
--        (1, '/path1', 200, 'content'),
--        (1, '/path2', 200, 'content'),
--        (1, '/path3', 200, 'content');