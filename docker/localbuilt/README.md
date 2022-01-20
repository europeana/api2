# Europeana Record API: Docker image builder

This will build a Docker image from an api.war already built locally.

## 1. Copy api.war
Copy the built api.war file to the same directory as where this file is located
Make sure the used api.war doesn't already contain any credentials!
```shell
cp ../../api2-war/target/api.war ./
```

## 2. Set version (optional for Maven builds)
Set which Record API version you want the docker image to have
This variable will be automatically set in Jenkins Maven project builds.
```shell
export POM_VERSION=2.14.1-SNAPSHOT
```

## 3. Build
Go to the folder where this file is located and run
```shell
docker build -t europeana/record-api:${POM_VERSION} .
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
           -e GOOGLE_APPLICATION_CREDENTIALS=/tmp/google_cloud_credentials.json \
		   -v /path/to/local/credential/file.json:/tmp/google_cloud_credentials.json:ro \
           europeana/record-api:${POM_VERSION}
```

## 5. Publish (optional)
Login to docker hub and publish the container
```shell
docker push europeana/record-api:${POM_VERSION}
```
