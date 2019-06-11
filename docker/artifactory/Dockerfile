FROM tomcat:9.0-jdk8-slim

MAINTAINER Europeana Foundation <development@europeana.eu>

ARG url=https://artifactory.eanadev.org
ARG path=/artifactory/libs-release-local/eu/europeana/api2/api2-war/
ARG version

COPY ./env-catalina.sh ./bin/
COPY ./europeana.env.properties ./webapps/api/europeana.env.properties

RUN apt-get update && \
    # gettext-base for envsubst, used by env-catalina.sh
    apt-get install -yq --no-install-recommends curl gettext-base && \
    curl -f -o webapps/api.war ${url}${path}${version}/api2-war-${version}.war && \
    apt-get remove -y -q --purge curl && \
    apt-get autoremove -y -q && \
    rm -rf /var/lib/apt/lists/* && \
    rm -r webapps/examples && \
    # Pre-extract the war so that *.properties may be replaced by env-catalina.sh
    # ... at the cost of a bigger image.
    cd webapps/api && \
    jar -xvf ../api.war && \
    rm ../api.war

ENTRYPOINT ["env-catalina.sh"]
CMD ["catalina.sh", "run"]
