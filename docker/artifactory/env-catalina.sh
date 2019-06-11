#! /bin/sh
envsubst < webapps/api/europeana.env.properties > webapps/api/WEB-INF/classes/europeana.properties
rm webapps/api/europeana.env.properties
exec "$@"
