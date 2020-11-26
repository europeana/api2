FROM tomcat:9.0-jdk11-slim

MAINTAINER Europeana Foundation <development@europeana.eu>

ARG url=https://artifactory.eanadev.org
ARG path=/artifactory/libs-release-local/eu/europeana/api2/api2-war/
ARG version

# Add script to inject properties defined in .env file on start-up
COPY ./env-catalina.sh ./bin/
COPY ./europeana.env.properties ./webapps/api/europeana.env.properties

# Get war from Artifactory and unpack
RUN apt-get update && apt-get install -yq --no-install-recommends \
    # Install curl and gettext-base (for envsubst used by env-catalina.sh)
    # Update ca-certificates to prevent problem with curl thinking artifactory has an invalid certificate
    ca-certificates \
    curl \
    gettext-base && \
    curl -f -o webapps/api.war ${url}${path}${version}/api2-war-${version}.war && \
    apt-get remove -y -q --purge curl && \
    apt-get autoremove -y -q && \
    # Clean-up
    rm -rf /var/lib/apt/lists/* && \
    rm -r webapps/examples && \
    rm -r webapps/docs && \
    # Extract the war so that *.properties may be replaced by env-catalina.sh (at the cost of a bigger image)
    cd webapps/api && \
    jar -xvf ../api.war && \
    rm ../api.war

ENTRYPOINT ["env-catalina.sh"]
CMD ["catalina.sh", "run"]
