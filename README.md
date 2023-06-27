# OpenAIRE CRIS validator

A tool to assess whether an OAI-PMH endpoint can provide research information
complying with the [OpenAIRE Guidelines for CRIS Managers](https://github.com/openaire/guidelines-cris-managers) version 1.1 or higher.
It covers [these checks](CHECKS.md).

This is a command-line Java tool that is organized as a [JUnit](https://junit.org/junit4/) test suite.
You can also run it in your IDE.

Please read below how to [build](#build) it, [run](#run) it, [explore](#internals) its internals and give [feedback](#feedback).
This is Open Source software, available under the terms of the [Apache 2.0 License](#license).


![CI workflow](https://github.com/euroCRIS/openaire-cris-validator/actions/workflows/maven.yml/badge.svg)
← checking if the software builds and runs on the [example files from the standard](./samples).

## Usage

### Build

Please make sure you have checked out the [guidelines-cris-managers](https://github.com/openaire/guidelines-cris-managers) project in a parallel directory.
Then do:

	mvn clean package

We compile for Java 17 by default, but you can switch to 11 or 1.8 [in the POM file](./pom.xml#L16).

### Run

#### From command line

	java -jar target/openaire-cris-validator-*-jar-with-dependencies.jar {endpoint-url}

#### From Eclipse

Set up a JUnit launcher for the `CRISValidator` class.
Pass the OAI-PMH endpoint URL as the value of the system property `endpoint.to.validate`.
Add the parallel `guidelines-cris-managers` project to the classpath of the launcher (in order to access the XML Schemas).

### Checking the examples from OpenAIRE Guidelines for CRIS Managers

Use `file:samples/` as your endpoint-url.

### Diagnostics

The validator copies the responses to the requests it makes into files in the `data/` subdirectory.


## Internals

[CRISValidator](./src/main/java/org/eurocris/openaire/cris/validator/CRISValidator.java) is the main validator class.  It is the JUnit4 test suite. 
As it reads the metadata records from the CRIS:
 * it does simple checks on the fly (using [CheckingIterable](./src/main/java/org/eurocris/openaire/cris/validator/util/CheckingIterable.java)); and
 * it builds an internal representation: a [HashMap](https://devdocs.io/openjdk~17/java.base/java/util/hashmap) of trees that consist of [CERIFNode](./src/main/java/org/eurocris/openaire/cris/validator/tree/CERIFNode.java)s. The last test, `check990_CheckReferentialIntegrityAndFunctionalDependency`, works on this internal representation.

[OAIPMHEndpoint](./src/main/java/org/eurocris/openaire/cris/validator/OAIPMHEndpoint.java) is an independent implementation
of an [OAI-PMH 2.0](https://www.openarchives.org/OAI/openarchivesprotocol.html) client in Java.
While it uses JAXB to map the OAI-PMH 2.0 markup to Java objects, any metadata payload is opaque to it.
For requests that list objects (i.e., `ListIdentifiers`, `ListRecords` or `ListSets`) an [Iterable](https://devdocs.io/openjdk~17/java.base/java/lang/iterable) is returned
that uses the protocol's resumption token mechanism to fetch successive chunks of objects.
This is entirely transparent to the class user. 

If the OAI-PMH 2.0 data provider advertises support for a compression, the endpoint client object will use it.
[CompressionHandlingHttpURLConnectionAdapter](./src/main/java/org/eurocris/openaire/cris/validator/http/CompressionHandlingHttpURLConnectionAdapter.java) is a transparent compression-handling wrapper around an [HttpURLConnection](https://devdocs.io/openjdk~17/java.base/java/net/httpurlconnection).


## Feedback

I'll be grateful for your feedback.  
Please submit a [github issue](https://github.com/euroCRIS/openaire-cris-validator/issues) or email me to [jan.dvorak@ff.cuni.cz](mailto:jan.dvorak@ff.cuni.cz).


## License

Copyright 2018–2022 Jan Dvořák <a href="https://orcid.org/0000-0001-8985-152X" target="orcid.widget" rel="noopener noreferrer" style="vertical-align:top;"><img src="https://orcid.org/sites/default/files/images/orcid_16x16.png" style="width:1em;margin-right:.5em;" alt="ORCID iD icon"> https://orcid.org/0000-0001-8985-152X</a> and other contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
