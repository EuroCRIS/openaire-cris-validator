# The checks performed by the OpenAIRE CRIS validator

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
(d) The `baseURL` from the `Identify` response is equal to the base URL of the CRIS.

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

