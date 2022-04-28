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
  Total:	30.0069 secs
  Slowest:	0.1508 secs
  Fastest:	0.0004 secs
  Average:	0.0073 secs
  Requests/sec:	13692.4227

Latency distribution:
  95% in 0.0135 secs
  99% in 0.0187 secs
```  

>Note: For best performance run scripts `./build-and-run.sh` and `tests/perf.sh` from terminal
