# OpenAIRE CRIS validator

A tool to assess whether an OAI-PMH endpoint can provide research information
complying with the [OpenAIRE Guidelines for CRIS Managers 1.1](https://github.com/openaire/guidelines-cris-managers).
It covers [these checks](CHECKS.md).

This is a command-line Java tool that is organized as a [JUnit](https://junit.org/junit4/) test suite.
You can also run it in your IDE.

Please read below how to [build](#build) it, [run](#run) it, [explore](#internals) its internals and give [feedback](#feedback).
This is Open Source software, available under the terms of the [Apache 2.0 License](#license).


## Current status 

[![Travis-CI Build Status](https://travis-ci.org/jdvorak001/openaire-cris-validator.svg?branch=master)](https://travis-ci.org/jdvorak001/openaire-cris-validator)
← checking if the software builds and runs on the [example files from the standard](https://github.com/openaire/guidelines-cris-managers/tree/master/samples).


## Releases

[1.0.0](../../releases/tag/v1.0.0) on 2018-05-22:
* checks fully implemented;
* works against a real CRIS.


## Usage

### Build

Please make sure you have checked out the [guidelines-cris-managers](https://github.com/openaire/guidelines-cris-managers) project in a parallel directory.
Then do:

	mvn clean package

### Run

#### From command line

	java -jar target/openaire-cris-validator-*-jar-with-dependencies.jar {endpoint-url}

With Java 9 you'll need the additional command-line option `--add-modules=java.xml.bind` to run the code (as per [this answer on StackOverflow](https://stackoverflow.com/a/43574427/7739289)).

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
As it reads the metadata records from the CRIS, it builds an internal representation:
a [HashMap](https://docs.oracle.com/javase/8/docs/api/java/util/HashMap.html) of trees that consist of [CERIFNode](./src/main/java/org/eurocris/openaire/cris/validator/tree/CERIFNode.java)s.
The last test, `check990_CheckReferentialIntegrityAndFunctionalDependency`, works on this internal representation.

[OAIPMHEndpoint](./src/main/java/org/eurocris/openaire/cris/validator/OAIPMHEndpoint.java) is an independent implementation of an OAI-PMH 2.0 client in Java.
While it uses JAXB to map the OAI-PMH 2.0 markup to Java objects, it does not make any assumptions about the metadata payload.
For requests list objects (i.e., `ListIdentifiers`, `ListRecords` or `ListSets`) an [Iterable](https://docs.oracle.com/javase/8/docs/api/java/lang/Iterable.html) is returned
that uses the protocol's resumption token mechanism to fetch successive chunks of objects.
This is entirely transparent to the class user. 

If the OAI-PMH 2.0 data provider advertises support for a compression, the endpoint client object will use it.
[CompressionHandlingHttpURLConnectionAdapter](./src/main/java/org/eurocris/openaire/cris/validator/http/CompressionHandlingHttpURLConnectionAdapter.java) is a transparent compression-handling wrapper around an [HttpURLConnection](https://docs.oracle.com/javase/8/docs/api/java/net/HttpURLConnection.html).


## Feedback

I'll be grateful for your feedback.  
Please submit a [github issue](https://github.com/jdvorak001/openaire-cris-validator/issues) or email me to [jan.dvorak@ff.cuni.cz](mailto:jan.dvorak@ff.cuni.cz).


## License

Copyright 2018 Jan Dvořák <a href="https://orcid.org/0000-0001-8985-152X" target="orcid.widget" rel="noopener noreferrer" style="vertical-align:top;"><img src="https://orcid.org/sites/default/files/images/orcid_16x16.png" style="width:1em;margin-right:.5em;" alt="ORCID iD icon"> https://orcid.org/0000-0001-8985-152X</a>

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
