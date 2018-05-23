# OpenAIRE CRIS validator

A tool to assess whether an OAI-PMH endpoint can provide research information
complying with the [OpenAIRE Guidelines for CRIS Managers 1.1](https://github.com/openaire/guidelines-cris-managers).
It currently covers [all of the checks](CHECKS.md).

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
