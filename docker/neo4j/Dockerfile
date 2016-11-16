FROM frodenas/java7
MAINTAINER Europeana.eu <jeroenjeurissen@gmail.com>

# Install and configure Neo4j 2.1.5
RUN  cd /tmp && \
     wget http://dist.neo4j.org/neo4j-community-2.1.5-unix.tar.gz && \
     tar xzvf neo4j-community-2.1.5-unix.tar.gz && \
     mv /tmp/neo4j-community-2.1.5/ /neo4j && \
#     sed -e 's/^org.neo4j.server.database.location=.*$/org.neo4j.server.database.location=\/data\/graph.db/' -i /neo4j/conf/neo4j-server.properties && \
     sed -e 's/^#org.neo4j.server.webserver.address=.*$/org.neo4j.server.webserver.address=0.0.0.0/' -i /neo4j/conf/neo4j-server.properties && \
     sed -e 's/^#org.neo4j.server.thirdparty_jaxrs.*$/org.neo4j.server.thirdparty_jaxrs_classes=eu.europeana.neo4j.count=\/europeana,eu.europeana.neo4j.initial=\/initial,eu.europeana.neo4j.delete=\/delete,eu.europeana.corelib.ordering=\/order,eu.europeana.neo4j.fetch=\/fetch/' -i /neo4j/conf/neo4j-server.properties && \
     sed -e '/^#wrapper.java.additional=-XX:+PrintTenuringDistribution.*$/a\\n# Java Additional Parameters\nwrapper.java.additional=-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=5005,server=y,suspend=n' -i /neo4j/conf/neo4j-wrapper.conf
COPY plugins/* /neo4j/plugins/
COPY data/graph.db.tar.gz /neo4j/data/
#    apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*
RUN  tar xzvf /neo4j/data/graph.db.tar.gz -C /neo4j/data/ && apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# Add scripts
ADD scripts /scripts
RUN chmod +x /scripts/*.sh

# Command to run
ENTRYPOINT ["/scripts/run.sh"]
CMD [""]

# Expose listen port
EXPOSE 7474 5005

# Expose our data volumes
VOLUME ["/data"]