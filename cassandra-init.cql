CREATE KEYSPACE IF NOT EXISTS ks1 WITH replication = {'class':'SimpleStrategy', 'replication_factor':1};
CREATE TABLE IF NOT EXISTS ks1.short_url(short_url_path text PRIMARY KEY, original_url text);
