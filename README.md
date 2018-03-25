# openaire-cris-validator

A validator tool to assess whether an OAI-PMH endpoint can provide research information
in compliance to the OpenAIRE Guidelines for CRIS Managers 1.1 <https://github.com/openaire/guidelines-cris-managers>.

## Usage

	./openaire-cris-validator.py *endpoint-url*


## Checks

The meaning of the SHALL keyword is specified in [RFC 2119](https://www.ietf.org/rfc/rfc2119.txt).

(0) Any XML response returned by the endpoint to the requests specified below SHALL validate with respect to the following XML Schemas:    
<http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd> for the namespace <http://www.openarchives.org/OAI/2.0/>,
<http://www.openarchives.org/OAI/2.0/oai-identifier.xsd> for the namespace <http://www.openarchives.org/OAI/2.0/oai-identifier> and       
<https://raw.githubusercontent.com/openaire/guidelines-cris-managers/master/schemas/openaire-cerif-profile.xsd> for the namespace <https://www.openaire.eu/cerif-profile/1.1/>.

(1) The response to an `Identify` request SHALL include:
exactly one `description` element that contains an `oai-identifier` from namespace <http://www.openarchives.org/OAI/2.0/oai-identifier> and
exactly one `description` element that contains a `Service` element from namespace <https://www.openaire.eu/cerif-profile/1.1/>.
The `oai-identifier/repositoryIdentifier` will be refered to as `{CRIS_identifier}` in the sequel.

(2) The list of supported metadata formats returned by the general `ListMetadataFormats` request (i.e., no `identifier` parameter specified) SHALL include
`oai_cerif_openaire` with namespace <https://www.openaire.eu/cerif-profile/1.1/>
as per [specification](http://openaire-guidelines-for-cris-managers.readthedocs.io/en/latest/implementation.html#metadata-format-and-prefix).

(3) The list of supported sets returned by the `ListSets` request SHALL include
all of the 10 sets as per [specification](http://openaire-guidelines-for-cris-managers.readthedocs.io/en/latest/implementation.html#openaire-oai-pmh-sets).

(4) The request for Service records (the `ListRecords` request with `metadataPrefix=oai_cerif_openaire` and `set=openaire_cris_services` parameters) SHALL retrieve
exactly one record; this record is identical to the one returned within the `Identify` request, see above.

(5) When all objects from the 10 sets as per [specification](http://openaire-guidelines-for-cris-managers.readthedocs.io/en/latest/implementation.html#openaire-oai-pmh-sets)
are retrieved and put together, the following statements SHALL hold:    
(a) Any `id` attribute in the CERIF XML markup points at an OAI record with identifier constructed as per [specification](http://openaire-guidelines-for-cris-managers.readthedocs.io/en/latest/implementation.html#oai-identifiers).
(b) CERIF XML markup contains contains no conflicts in properties: where a property value is given, the value does not differ from that in other places where the value of the same property is given.



## License

Copyright 2018 Jan Dvořák (https://orcid.org/0000-0001-8985-152X)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
