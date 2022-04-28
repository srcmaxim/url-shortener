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
smoke/local.sh
```
