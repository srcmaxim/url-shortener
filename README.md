# URL Shortener

A url shortener is a component that, given an arbitrary long URL creates a shortened version.

> Java: 17
> Kotlin: 1.6.10
> Quarkus: 2.8
> GraalVM: 22 (optional)

The app can be compiled to native-image that saves a lot of memory and startup time.
For more info go to `RUN.md`.

```bash
curl -X POST -H 'Content-Type: application/json' \
  -d 'https://www.youtube.com/watch?v=dQw4w9WgXcQ' http://localhost:8080/shorten; echo
```

## Functional Requirements

1. Support a basic SEO url shortener. Give a customer the possibility to have a custom SEO keyword.

```bash
curl -X POST -H 'Content-Type: application/json' \
  -d 'https://www.youtube.com/watch?v=dQw4w9WgXcQ' http://localhost:8080/shorten?customAlias=NeverGiveUp; echo
```

3. Support a generic url shortener. Shorten to random 6 chars string that can be made by letters and numbers.

```bash
curl -X POST -H 'Content-Type: application/json' \
  -d 'https://www.youtube.com/watch?v=dQw4w9WgXcQ' http://localhost:8080/shorten; echo
```

3. Retrieve original URL given a SEO/Random shortened URL. Give a customer the possibility to know the original URL given a shortened one.

```bash
export SHORT=$(curl -X POST -H 'Content-Type: application/json' \
  -d 'https://www.youtube.com/watch?v=dQw4w9WgXcQ' http://localhost:8080/shorten)
curl $SHORT  
```

4. Collision Preventing. Detect collisions during URL creation phase for SEO URL and randomly generated one.

```bash
curl -X POST -H 'Content-Type: application/json' \
  -d 'https://www.youtube.com/watch?v=dQw4w9WgXcQ' http://localhost:8080/shorten?customAlias=A; echo
curl -X POST -H 'Content-Type: application/json' \
  -d 'https://www.youtube.com/watch?v=dQw4w9WgXcQ' http://localhost:8080/shorten?customAlias=A; echo  
```

6. Support Time to live. Use time to live that will remove the shortened URL entry.

```bash
curl -X POST -H 'Content-Type: application/json' \
  -d 'https://www.youtube.com/watch?v=dQw4w9WgXcQ' http://localhost:8080/shorten?ttl=100; echo
```

## Non-Functional Requirements

1. The system should be highly available. This is required because, if our service is down, all the URL redirections will start failing.

2. URL redirection should happen in real-time with minimal latency.

3. Shortened links should not be guessable (not predictable).

## System Design

### Capacity Planing

1. Traffic Estimate (local network)
   That's a ballpark estimate but 10_000 RPS will be enough for in-memory storage on local network
2. Storage estimates (100_000 URLs)
   Storing Original URLs:
     - 1 URL ~ 45 chars = 45 bytes (with Java String ASCII compaction)
     - 100_000 URLs ~ 4_500_000 bytes = 4.5 MB
   Storing Short URL Paths:
     - 1 URL ~ 10 chars = 10 bytes (with Java String ASCII compaction)
     - 100_000 URLs ~ 4_500_000 bytes = 1 MB
   Storing TTL time to expire:
     - 1 URL with TTLs ~ 8 bytes
     - 100_000 URLs with TTLs ~ 0.8 MB
   Fast BitSet for quick Short Base64 URL check:  
     - 1 Short Base64 URL = log2(64^6) = 24 bits = int
     - 100_000 Short Base64 URL = max 8.5 GB with RoaringBitmap implementation
3. Bandwidth estimates (10_000 URLs per second)
     - 10_000 RPS * 45 bytes = 450 KB per second = 1.62 GB per hour
4. Memory estimates (100_000 URLs)
     - the same as Storage estimates

### System API

We'll use REST API to expose the functionality of our service.

Create URL: `createURL(original_url, custom_alias=None, ttl=None) return`

>original_url (string): Original URL to be shortened.
custom_alias (string): Optional custom key for the URL.
tll (string): Optional expiration time in milliseconds for the shortened URL.
return (string): Shortened URL.

### Database Design

A few observations about the nature of the data we will store:
- We need to store billions of records.
- Each object we store is small (less than 1K).
- There are no relationships between records.
- Our service is read-heavy.
- Use TTL to delete records.

Database Schema:

| hash(short_url_path)             | original_url | ttl          |
|----------------------------------|--------------|--------------|
| Shortened URL Path, like: Ns1El4 | Original URL | milliseconds |

What kind of database should we use?
Considering that we are read-heavy and we need to store a lot of rows and there's no relationships b/w objects.
Using NoSQL DBs is a great option. We can use key-value or wide-column storage like DynamoDB or Cassandra.

In this code we will use in memory key-value data structure `ConcurentHashMap` with own expiration strategy.

### Basic Algorithms

#### Encoding URL

If we use 6 characters with Base64 code that's 64^6 = ~68.7 billion possible strings.

#### Collision Prevention

We can use insert in DB with collision check, so now we can detect collisions.
Then when collision happens we can just respond in desired way.
In NoSQL DB like Cassandra this can look like:

```sql
CREATE KEYSPACE k1 WITH replication = {'class':'SimpleStrategy', 'replication_factor':1};
CREATE TABLE k1.short_url(short_url_path text PRIMARY KEY, original_url text);
INSERT INTO k1.short_url (short_url_path, original_url) values ('NeverGiveUp', 'https://www.youtube.com/watch?v=dQw4w9WgXcQ') USING TTL 86400 IF NOT EXISTS;
SELECT * FROM  k1.short_url where short_url_path = 'NeverGiveUp';
```

### Caching

Our system is read heavy, that's why we need cache every redirect URL.
We can use off the shelf solution like Caffeine Cache.

How much cache memory should we have?
Let's cache 10% of 100_000 requests, that's 10_000 Short URL Paths and OriginalUrls.
5.5 MB * 10% ~ 0.6 MB

Which cache eviction policy would best fit our needs?
Default LRU policy with Expire After Access. 

Which cache strategy use we can use?
Read through cache option will be sufficient. With this setup we'll read every value from cache.
If there's no value it will be retrieved from DB.

Do we need to worry if Cache doesn't expire on time, and we serve stale values?
No one is going to be killed if we show expire cache value for 1 minute.

>Note: for demonstration purposes cache time is set to 1 second
