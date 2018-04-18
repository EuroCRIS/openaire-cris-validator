# OpenAIRE CRIS validator

A tool to assess whether an OAI-PMH endpoint can provide research information
in compliance to the OpenAIRE Guidelines for CRIS Managers 1.1 <https://github.com/openaire/guidelines-cris-managers>.


## The checks

The meaning of the SHALL keyword is specified in [RFC 2119](https://www.ietf.org/rfc/rfc2119.txt).

(0) Any XML response returned by the endpoint to the requests specified below SHALL validate with respect to the following XML Schemas:    
<http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd> for the namespace <http://www.openarchives.org/OAI/2.0/>,   
<http://www.openarchives.org/OAI/2.0/oai-identifier.xsd> for the namespace <http://www.openarchives.org/OAI/2.0/oai-identifier> and       
<https://raw.githubusercontent.com/openaire/guidelines-cris-managers/master/schemas/openaire-cerif-profile.xsd> for the namespace <https://www.openaire.eu/cerif-profile/1.1/>.

(1) The response to an `Identify` request SHALL include:  
(a) exactly one `description` element that contains a `Service` element from namespace <https://www.openaire.eu/cerif-profile/1.1/> and    
(b) exactly one `description` element that contains an `oai-identifier` element from namespace <http://www.openarchives.org/OAI/2.0/oai-identifier>.   
The `oai-identifier/repositoryIdentifier` from (b) will be refered to as `{CRIS_identifier}` in the sequel.  
(c) The `Service/Acronym` from (a) SHALL be equal to the `{CRIS_identifier}`.

(2) The list of supported metadata formats returned by the general `ListMetadataFormats` request (i.e., no `identifier` parameter specified) SHALL include
`oai_cerif_openaire` with namespace <https://www.openaire.eu/cerif-profile/1.1/>
as per [specification](http://openaire-guidelines-for-cris-managers.readthedocs.io/en/latest/implementation.html#metadata-format-and-prefix).

(3) The list of supported sets returned by the `ListSets` request SHALL include
all of the sets as per the [specification](http://openaire-guidelines-for-cris-managers.readthedocs.io/en/latest/implementation.html#openaire-oai-pmh-sets).

(4) - removed

(5) When all objects from the sets as per the [specification](http://openaire-guidelines-for-cris-managers.readthedocs.io/en/latest/implementation.html#openaire-oai-pmh-sets)
are retrieved using the `ListRecords` requests and put together, the following statements SHALL hold:    
(a) Any `id` attribute in the CERIF XML markup points at an OAI record with identifier constructed as per [specification](http://openaire-guidelines-for-cris-managers.readthedocs.io/en/latest/implementation.html#oai-identifiers), including the `{CRIS_identifier}`.  
(b) CERIF XML markup contains no conflicts in properties: where a property value is given, the value does not differ from that in other places where the value of the same property is given.


## Current status 
(as of 2018-04-17)

The software currently covers checks (0), (1b), (2) and (3) above.
For (5), it tries to call `ListRecords` on all the sets and validates what comes, but does not do the more specific checks yet.

The validator copies the responses to the requests it makes in files in the `data/` subdirectory.


## Usage

### Build

Please make sure you have checked out the `guidelines-cris-managers` project in a parallel directory.
Then do:

	mvn clean compile package

### Run

#### From command line

	java -jar target/openaire-cris-validator-*-jar-with-dependencies.jar {endpoint-url}

#### From Eclipse

Set up a JUnit launcher for the `CRISValidator` class.
Pass the OAI-PMH endpoint URL as the value of the system property `endpoint.to.validate`.
Add the parallel `guidelines-cris-managers` project to the classpath of the launcher (in order to access the XML Schemas).


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
