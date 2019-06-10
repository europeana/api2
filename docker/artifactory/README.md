# Europeana Record API: Docker image builder

This will build a Docker image from a version of API2 already built and
published to Artifactory.

## Version
``shell
export VERSION=2.8.7
```

## Build
```shell
docker build -t europeana/record-api:${VERSION} \
             --build-arg version=${VERSION} .
```

## Configure
* Copy .env.example to .env
* Fill out the settings in .env

## Run
```shell
docker run -p 8080:8080 \
           --env-file=.env \
           europeana/record-api:${VERSION}
```

## Publish
```shell
docker push europeana/record-api:${VERSION}
```
