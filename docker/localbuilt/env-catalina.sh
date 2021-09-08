#! /bin/sh
# Script to generate europeana.properties file in Docker container from europeana.env.properties file
envsubst < webapps/api/europeana.env.properties > webapps/api/WEB-INF/classes/europeana.properties
rm webapps/api/europeana.env.properties
exec "$@"
