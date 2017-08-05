![](https://github.com/europeana/portal/blob/master/portal2/src/main/webapp/themes/default/images/europeana-logo-retina-1.png)

# Europeana: catalyst for change in the world of cultural heritage
[![Build Status](https://travis-ci.org/europeana/api2.svg?branch=master)](https://travis-ci.org/europeana/api2)[![Coverage Status](https://coveralls.io/repos/europeana/api2/badge.svg?branch=master&service=github)](https://coveralls.io/github/europeana/api2?branch=master)

## What does it do?

Europeana is an internet portal that acts as an interface to millions of books, paintings, films, museum objects and archival records that have been digitised throughout Europe. The API provides the machine interface for accessing these digital objects.

## Full Documentation

See the [Wiki](https://github.com/europeana/api2/wiki) for full documentation, examples, operational details and other information.

The JavaDoc will be generated once the upcoming code overhaul is complete.

## Communication

- Google Group: [Europeana API forum](https://groups.google.com/d/forum/europeanaapi)
- [GitHub Issues](https://github.com/europeana/api2/issues)

## Build
To build (requires the [CoreLib](https://github.com/europeana/corelib) dependency):

Configure your maven settings: http://artifactory.eanadev.org/artifactory/webapp/mavensettings.html?1

```bash
$ git clone https://github.com/europeana/corelib.git
$ cd corelib
$ mvn clean install

$ git clone https://github.com/europeana/api2.git
$ cd api2
$ mvn clean install
```

## Deploy
To deploy your instance you can use the docker files in docker folder. This image contains Tomcat, Postgresql and Neo4j 
database server. However at the moment we do not have a Mongo database, Solr engine, mail server and object storage 
provider in Docker yet (last 2 are optional).

The addresses and login credentials of all these services are specified in the europeana.properties file located in the
/api2/api2-war/src/main/resources/ folder. For the moment you still need to fill in all the 'REMOVED' values (login 
credentials for services that are not dockerized yet). **Make sure you never commit these changes!**
It's safer to place these login credentials in a europeana.user.properties file in the same folder because this file
is set to be ignored by git. All settings in the europeana.user.properties will override those in the europeana.properties.


## LICENSE

Copyright 2007-2017 The Europeana Foundation

Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved by the European Commission;
You may not use this work except in compliance with the Licence.

You may obtain a copy of the Licence at: [http://joinup.ec.europa.eu/software/page/eupl](http://joinup.ec.europa.eu/software/page/eupl)

Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis, without warranties or conditions of any kind, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
