# URL Shortener

> Java: 17
> Kotlin: 1.6.10
> Quarkus: 2.8
> GraalVM: 22 (optional)

For more info on Quarkus go to `README_QUARKUS.md`.

## Build and run application from docker-compose (preferred for native mode)

You can build and run your application from docker-compose:
```shell script
./build-and-run.sh
```

This will create GraalVM native-image compilation.

## Running the application in dev mode (preferred for local development)

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

## See other run configs in README_QUARKUS.md 

## Run smoke tests

```shell script
tests/smoke.sh
```

You'll see these checks:

```
     ✓ /shorten
     ✓ /shorten ttl
     ✓ /shorten customAlias
     ✓ /shorten customAlias ttl
     ✓ /shorten customAlias duplicate
     ✓ /redirect expire
```     

## Run performance tests

```shell script
tests/perf.sh
```

You'll see this summary:

```
Summary:
  Total:        30.0046 secs
  Slowest:      0.1692 secs
  Fastest:      0.0009 secs
  Average:      0.0138 secs
  Requests/sec: 3525.3231
     
Latency distribution:
  95% in 0.0585 secs
  99% in 0.0842 secs
```  
