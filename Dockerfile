# Builds a docker image from the Maven war. Requires 'mvn package' to have been run beforehand
FROM tomcat:9.0-jre17
LABEL Author="Europeana Foundation <development@europeana.eu>"
WORKDIR /usr/local/tomcat/webapps

ENV ELASTIC_APM_VERSION 1.34.1
RUN wget https://repo1.maven.org/maven2/co/elastic/apm/elastic-apm-agent/$ELASTIC_APM_VERSION/elastic-apm-agent-$ELASTIC_APM_VERSION.jar -O /usr/local/elastic-apm-agent.jar

# Copy unzipped directory so we can mount config files in Kubernetes pod
COPY api2-war/target/api/ ./ROOT/

# Remove properties file and credentials. Don't fail if either file is missing
RUN rm -f ./ROOT/WEB-INF/classes/europeana.user.properties

# Uncomment the options below and map port number 9010 to enable profiling in the docker container
#EXPOSE 8080
#EXPOSE 9010
#ENV CATALINA_OPTS="-Dcom.sun.management.jmxremote \
#-Dcom.sun.management.jmxremote.port=9010 \
#-Dcom.sun.management.jmxremote.local.only=false \
#-Dcom.sun.management.jmxremote.authenticate=false \
#-Dcom.sun.management.jmxremote.ssl=false"


