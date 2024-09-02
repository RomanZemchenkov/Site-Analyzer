INSERT INTO site(status, status_time, url, name)
VALUES('INDEXED','2000-02-02 00:00:00.000+00','/test/url','Test Site');


INSERT INTO page(site_id, path, code, content)
VALUES (1,'/path',200,'content'),
       (1,'/path1',200,'content'),
       (1,'/path2',200,'content'),
       (1,'/path3',200,'content');