# Europeana Record API: Docker image builder

This will build a Docker image from a version of API2 already built and
published to Artifactory.

## 1. Set version
Set which Record API version you want to use
```shell
export VERSION=2.11.0
```

## 2. Build
Go to the folder where this file is located and run
```shell
docker build -t europeana/record-api:${VERSION} \
             --build-arg version=${VERSION} .
```

## 3. Configure
The .env file is used when you start-up the Docker image to generate and inject an europeana.properties file
* Copy .env.example to .env
* Fill out the settings in .env

## 4. Run
Start docker container with application listening on port 8080
```shell
docker run -p 8080:8080 \
           --env-file=.env \
           europeana/record-api:${VERSION}
```

## 5. Publish (optional)
Login to docker hub and publish the container
```shell
docker push europeana/record-api:${VERSION}
```
