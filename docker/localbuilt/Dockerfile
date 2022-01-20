# This docker file pulls an api.war file from Artifactory and builds a docker image

FROM tomcat:9.0-jdk11-slim

MAINTAINER Europeana Foundation <development@europeana.eu>

# Add script to inject properties defined in .env file on start-up
COPY ./env-catalina.sh ./bin/
COPY ./europeana.env.properties ./webapps/api/europeana.env.properties
COPY ./api.war ./webapps/api.war

# Get war from Artifactory and unpack
RUN apt-get update && apt-get install -yq --no-install-recommends \
    # Install gettext-base (for envsubst used by env-catalina.sh)
    gettext-base && \

    # Clean-up
    apt-get autoremove -y -q && \
    rm -rf /var/lib/apt/lists/* && \
    rm -r webapps/examples && \
    rm -r webapps/docs && \

    # Extract the war so that *.properties may be replaced by env-catalina.sh (at the cost of a bigger image)
    cd webapps/api && \
    jar -xvf ../api.war && \
    rm ../api.war && \
    # Make sure we do not include credential files in the image
    rm -f ./europeana.user.properties && \
    rm -f ./google_cloud_credentials.json

ENTRYPOINT ["env-catalina.sh"]
CMD ["catalina.sh", "run"]
