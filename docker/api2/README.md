# Europeana Search API Docker image builder

This will build a Docker image from a version of API2 already built and
published to Artifactory.

## Build
```shell
export VERSION=2.8.7
docker build -f Dockerfile.artifactory \
             -t europeana/search-api:${VERSION} \
             --build-arg version=${VERSION} .
```

## Configure
* Copy .env.example to .env
* Fill out the settings in .env

## Run
```shell
export VERSION=2.8.7
docker run -p 8080:8080 \
           --env-file=.env \
           europeana/search-api:${VERSION}
```

## Publish
```shell
export VERSION=2.8.7
docker push europeana/search-api:${VERSION}
```
