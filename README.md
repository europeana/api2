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

```bash
$ git clone https://github.com/europeana/api2.git
$ cd api2/
```

If your instance will be deployed, you need to copy the file

properties/test/template-europeana.properties -> properties/test/europeana.properties

and

template-manifest.yml -> manifest.yml

And make necessary changes

```bash
$ mvn clean install -DskipTests
```

Futher details on building can be found on the [Deploy](https://github.com/europeana/api2/wiki/Deploy) page of the wiki (but check the [Setup](https://github.com/europeana/api2/wiki/Setup) first!.

## LICENSE

Copyright 2007-2016 The Europeana Foundation

Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved by the European Commission;
You may not use this work except in compliance with the Licence.

You may obtain a copy of the Licence at: [http://joinup.ec.europa.eu/software/page/eupl](http://joinup.ec.europa.eu/software/page/eupl)

Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis, without warranties or conditions of any kind, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.

## Thank you Jetbrains!

We've been granted an open source license to use [Jetbrains'](https://www.jetbrains.com) IDE's for our work on the Europeana code base:

![](https://raw.githubusercontent.com/Luthien-in-edhil/jetbrainsicons/master/icon_IntelliJIDEA.png) [IntelliJ] (https://www.jetbrains.com/idea/) Java IDE 

![](https://raw.githubusercontent.com/Luthien-in-edhil/jetbrainsicons/master/icon_PyCharm.png) [PyCHarm](https://www.jetbrains.com/pycharm/) Python IDE

![](https://raw.githubusercontent.com/Luthien-in-edhil/jetbrainsicons/master/icon_RubyMine.png) [RubyMine](https://www.jetbrains.com/ruby/) Ruby IDE 

![](https://raw.githubusercontent.com/Luthien-in-edhil/jetbrainsicons/master/icon_WebStorm.png) [WebStorm](https://www.jetbrains.com/webstorm/) JavaScript IDE 
