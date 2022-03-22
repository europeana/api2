# Builds a docker image from the Maven war. Requires 'mvn package' to have been run beforehand
FROM tomcat:9.0-jdk11

LABEL Author="Europeana Foundation <development@europeana.eu>"

WORKDIR /usr/local/tomcat/webapps

# Copy unzipped directory so we can mount config files in Kubernetes pod
COPY api2-war/target/api/ ./ROOT/

# Remove properties file and credentials. Don't fail if either file is missing
RUN rm -f ./ROOT/WEB-INF/classes/europeana.user.properties
RUN rm -f ./ROOT/WEB-INF/classes/google_cloud_credentials.json