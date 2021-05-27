#Using the Dockerized API development environment

You can use Docker to quickly setup an API development environment. The docker environment consists
only of a Tomcat server to deploy the API.war

We assume you have Docker already installed on your computer. You need docker-compose version 1.8.0 or later to 
be able to run it ([installation instructions here](https://github.com/docker/compose/releases)).

## Build the api
- Configure the api2/api2-war/src/main/resources/europeana.properties (fill in the username/passwords of external services)
- Run `maven install`
- Docker will use the files in the api/target folder

##Starting docker
- Go to the docker folder and execute the command: `docker-compose up`

##Usage:
 - If you press <kbd>Ctrl</kbd>+<kbd>C</kbd> in the terminal then docker will stop, preserving your current containers. You can restart by
   executing docker-compose up again. If you want to do a clean start you can throw your old containers away first with
   this command: `docker rm docker_appserver_1 `
 - For debugging use Java/Tomcat port = 8000
 - By default we enable SLL and re-route all http-requests to https.

##Favorite Docker commands:

**Start all API containers**: docker-compose up

**View all running containers**:
docker ps

**Restart Tomcat API application**:
docker restart docker_appserver_1

**Start all API containers in detached mode**:
docker-compose up -d

**Build all images**:
docker-compose build

**Shutdown and remove containers**:
docker-compose down

**Open bash inside a container**:
docker exec -i -t docker_appserver_1 /bin/bash

