#!/bin/bash

# Start Neo4j
echo "Starting Neo4j..."
ulimit -n 65536
/neo4j/bin/neo4j console
