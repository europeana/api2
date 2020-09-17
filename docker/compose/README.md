#Using the Dockerized API development environment

You can use Docker to quickly setup an API development environment. The docker environment consists of 2 components:
 - Tomcat server to deploy the API.war
 - Postgresql database
 
The postgresql database is prefilled with an api key (api2demo/verysecret) and testuser (test@test.com/test)

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
   this command: `docker rm docker_webserver_1 docker_appserver_1 docker_relational-database_1`
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
docker exec -i -t docker_relational-database_1 /bin/bash

**Start your container with environment parameters and self destruct after you stop it with ctrl-c on local port 5433**:
docker run -i -t -e POSTGRES_USER=europeana -e POSTGRES_PASSWORD=culture -p 5433:5432 --name test-postgres-config --rm api-postgresql-database

