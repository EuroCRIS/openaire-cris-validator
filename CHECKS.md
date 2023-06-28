# The checks performed by the OpenAIRE CRIS validator

The meaning of the SHALL keyword is specified in [RFC 2119](https://www.ietf.org/rfc/rfc2119.txt).

Upon startup, the validator constructs the table of supported OpenAIRE CRIS metadata schemas (the *TSOACMS* in the sequel).
It currently includes the following schemas:
| XML namespace URI                          | XML Schema source location                                                        |
|--------------------------------------------|-----------------------------------------------------------------------------------|
| https://www.openaire.eu/cerif-profile/1.1/ | [src/main/resources/schemas/cerif_profile_1_1/openaire-cerif-profile.xsd](./src/main/resources/schemas/cerif_profile_1_1/openaire-cerif-profile.xsd) |
| https://www.openaire.eu/cerif-profile/1.2/ | [schemas/openaire-cerif-profile.xsd](../../../../openaire/guidelines-cris-managers/blob/v1.2/schemas/openaire-cerif-profile.xsd) in the [OpenAIRE CRIS Guidelines project](../../../../openaire/guidelines-cris-managers) |

(0) Any XML response returned by the endpoint to the requests specified below SHALL validate with respect to the XML Schemas from the *TSOACMS* and the following XML Schemas:
| XML namespace URI | XML Schema source location |
|-------------------|----------------------------|
| http://www.openarchives.org/OAI/2.0/ | http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd |
| http://www.openarchives.org/OAI/2.0/oai-identifier | http://www.openarchives.org/OAI/2.0/oai-identifier.xsd |

(1) The response to an `Identify` request SHALL include:  
(a) exactly one `description` element that contains a `Service` element from a namespace from the *TSOACMS* and
(b) exactly one `description` element that contains an `oai-identifier` element from namespace <http://www.openarchives.org/OAI/2.0/oai-identifier>.   
The `oai-identifier/repositoryIdentifier` from (b) will be refered to as `{CRIS_identifier}` in the sequel.
(c) The `Service/Acronym` from (a) SHALL be equal to the `{CRIS_identifier}`.
(d) The `baseURL` from the `Identify` response is equal to the base URL of the CRIS.

(2) As per the [specification](http://openaire-guidelines-for-cris-managers.readthedocs.io/en/latest/implementation.html#metadata-format-and-prefix),
the list of supported metadata formats returned by the general `ListMetadataFormats` request (i.e., no `identifier` parameter specified) 
(a) SHALL include at least one prefix starting with `oai_cerif_openaire`;
(b) if the metadata prefix starts with `oai_cerif_openaire`, the corresponding XML namespace URI SHALL start with <https://www.openaire.eu/cerif-profile/>.
(c) If XML namespace URI starts with <https://www.openaire.eu/cerif-profile/>, the metadata prefix SHALL start `oai_cerif_openaire`.
(d) The metadata prefixes SHALL be unique.
(e) The XML namespace URIs SHALL be unique.
(f) The XML Schema location URLs SHALL be unique.
(g) If the XML namespace starts with <https://www.openaire.eu/cerif-profile/>, it shall be found in the *TSOACMS*.
(h) If the XML namespace starts with <https://www.openaire.eu/cerif-profile/>, the XML Schema location shall be under <https://www.openaire.eu/schema/cris/>.
(i) If the XML namespace starts with <https://www.openaire.eu/cerif-profile/>, the filename of the XML Schema shall be `openaire-cerif-profile.xsd`.
(j) The targetNamespace of a referenced XML Schema SHALL be the same as the one advertised for the metadata format in the `ListMetadataFormats` response.

(3) The list of supported sets returned by the `ListSets` request SHALL include
all of the sets as per the [specification](http://openaire-guidelines-for-cris-managers.readthedocs.io/en/latest/implementation.html#openaire-oai-pmh-sets).

(4) - removed

(5) When all objects from the sets and metadata formats as per the [specification](http://openaire-guidelines-for-cris-managers.readthedocs.io/en/latest/implementation.html#openaire-oai-pmh-sets)
are retrieved using the `ListRecords` requests and put together, the following statements SHALL hold:    
(a) Any `id` attribute in the CERIF XML markup points at an OAI record with identifier constructed as per [specification](http://openaire-guidelines-for-cris-managers.readthedocs.io/en/latest/implementation.html#oai-identifiers), including the `{CRIS_identifier}`.  
(b) CERIF XML markup contains no conflicts in properties: where a property value is given, the value does not differ from that in other places where the value of the same property is given.

