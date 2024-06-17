# Europeana: catalyst for change in the world of cultural heritage

## What does it do?

Europeana is an internet portal that acts as an interface to millions of books, paintings, films, museum objects and archival records that have been digitised throughout Europe. The API provides the machine interface for accessing these digital objects.

## Full Documentation

See the [Europeana Pro website](https://pro.europeana.eu/page/apis) for full documentation, examples, operational details and other information.

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

Deployment is done using the project's Dockerfile and Kubernetes configuration files in the k8s folder.

The addresses and login credentials of all required services are specified in the europeana.properties file located in the
/api2/api2-war/src/main/resources/ folder. You can fill in something to replace all the 'REMOVED' values, but the safest
way is to override the 'REMOVED' values using a `europeana.user.properties` file placed in the same folder. This file
is set to be ignored by git so won't be committed.

The kubernetes configuration files are templates containing environment variables. You can generate
a configuration file containing the correct variable values using for example

envsubst < $WORKSPACE/api2/k8s/overlays/canary/deployment_patch.properties.yaml.template > $WORKSPACE/api2/k8s/overlays/canary/deployment_patch.properties.yaml

## LICENSE

Licenced under the EUPL, Version 1.2 (the "Licence") and subsequent versions as approved by the European Commission;
You may not use this work except in compliance with the Licence.

You may obtain a copy of the Licence at: [http://joinup.ec.europa.eu/software/page/eupl](http://joinup.ec.europa.eu/software/page/eupl)

Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis, without warranties or conditions of any kind, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
