package org.eurocris.openaire.cris.validator;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.xml.bind.JAXBElement;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.cli.MissingArgumentException;
import org.eurocris.openaire.cris.validator.tree.CERIFNode;
import org.eurocris.openaire.cris.validator.util.CheckingIterable;
import org.eurocris.openaire.cris.validator.util.XmlUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runners.MethodSorters;
import org.openarchives.oai._2.DescriptionType;
import org.openarchives.oai._2.HeaderType;
import org.openarchives.oai._2.IdentifyType;
import org.openarchives.oai._2.MetadataFormatType;
import org.openarchives.oai._2.MetadataType;
import org.openarchives.oai._2.RecordType;
import org.openarchives.oai._2.SetType;
import org.openarchives.oai._2.StatusType;
import org.openarchives.oai._2_0.oai_identifier.OaiIdentifierType;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

@FixMethodOrder( value=MethodSorters.NAME_ASCENDING )
public class CRISValidator {
	
	public static final String OPENAIRE_CRIS_EQUIPMENTS__SET_SPEC = "openaire_cris_equipments";
	public static final String OPENAIRE_CRIS_EVENTS__SET_SPEC = "openaire_cris_events";
	public static final String OPENAIRE_CRIS_FUNDING__SET_SPEC = "openaire_cris_funding";
	public static final String OPENAIRE_CRIS_PROJECTS__SET_SPEC = "openaire_cris_projects";
	public static final String OPENAIRE_CRIS_ORGUNITS__SET_SPEC = "openaire_cris_orgunits";
	public static final String OPENAIRE_CRIS_PERSONS__SET_SPEC = "openaire_cris_persons";
	public static final String OPENAIRE_CRIS_PATENTS__SET_SPEC = "openaire_cris_patents";
	public static final String OPENAIRE_CRIS_PRODUCTS__SET_SPEC = "openaire_cris_products";
	public static final String OPENAIRE_CRIS_PUBLICATIONS__SET_SPEC = "openaire_cris_publications";
	
	public static final String OAI_CERIF_OPENAIRE__METADATA_PREFIX = "oai_cerif_openaire";
	
	public static final String OPENAIRE_CERIF_XMLNS = "https://www.openaire.eu/cerif-profile/1.1/";
	
	public static final String LOG_DIR = "data";

	public static void main( final String[] args ) throws Exception {
		final String endpointUrl = ( args.length > 0 ) ? args[0] : null;
		final URL endpointBaseUrl = new URL( endpointUrl );
		endpoint = new OAIPMHEndpoint( endpointBaseUrl, createParserSchema(), LOG_DIR );
		JUnitCore.main( CRISValidator.class.getName() );
	}
	
	private static OAIPMHEndpoint endpoint;

	public CRISValidator() throws MalformedURLException, MissingArgumentException, SAXException {
		if ( endpoint == null ) {
			final String endpointPropertyKey = "endpoint.to.validate";
			final String endpointUrl = System.getProperty( endpointPropertyKey );
			if ( endpointUrl == null ) {
				throw new MissingArgumentException( "Please specify the OAI-PMH endpoint URL as the value of the " + endpointPropertyKey + " system property or as the first argument on the command line" );
			}
			final URL endpointBaseUrl = new URL( endpointUrl );
			endpoint = new OAIPMHEndpoint( endpointBaseUrl, createParserSchema(), LOG_DIR );			
		}
	}
	
	public String getName() {
		return endpoint.getBaseUrl();
	}
	
	private static Schema schema = null;
	
	protected static synchronized Schema createParserSchema() throws SAXException {
		if ( schema == null ) {
			final SchemaFactory sf = SchemaFactory.newInstance( W3C_XML_SCHEMA_NS_URI );
			final Source[] schemas = { 
					schema( "/openaire-cerif-profile.xsd" ), 
					schema( "/cached/oai-identifier.xsd" ), 
					schema( "/cached/OAI-PMH.xsd" ), 
					schema( "/cached/oai_dc.xsd" ), 
					schema( "/cached/xml.xsd", "http://www.w3.org/2001/xml.xsd" ), 
				};
			schema = sf.newSchema( schemas );
		}
		return schema;
	}

	private static Source schema( final String path ) {
		return schema( path, null );
	}
	
	private static Source schema( final String path, final String externalUrl ) {
		final String path1 = "/schemas" + path;
		final URL url = OAIPMHEndpoint.class.getResource( path1 );
		if ( url == null ) {
			throw new IllegalArgumentException( "Resource " + path1 + " not found" );
		}
		final StreamSource src = new StreamSource();
		src.setInputStream( OAIPMHEndpoint.class.getResourceAsStream( path1 ) );
		src.setSystemId( ( externalUrl != null ) ? externalUrl : url.toExternalForm() );
		return src;
	}

	@SuppressWarnings( "unused")
	private static Optional<String> sampleIdentifier = Optional.empty();

	private static Optional<String> repoIdentifier = Optional.empty();
	
	private static Optional<String> serviceAcronym = Optional.empty();
	
	@Test
	public void check000_Identify() throws Exception {
		final IdentifyType identify = endpoint.callIdentify();
		CheckingIterable<DescriptionType> checker = CheckingIterable.over( identify.getDescription() );
		checker = checker.checkContainsOne( new Predicate<DescriptionType>() {

			@Override
			public boolean test( final DescriptionType description ) {
				final Object obj = description.getAny();
				if ( obj instanceof JAXBElement<?> ) {
					final JAXBElement<?> jaxbEl = (JAXBElement<?>) obj;
					final Object obj1 = jaxbEl.getValue();
					if ( obj1 instanceof OaiIdentifierType ) {
						final OaiIdentifierType oaiIdentifier = (OaiIdentifierType) obj1;
						sampleIdentifier = Optional.ofNullable( oaiIdentifier.getSampleIdentifier() );
						repoIdentifier = Optional.ofNullable( oaiIdentifier.getRepositoryIdentifier() );
						return true;
					}
				}
				return false;
			}
			
		}, "the Identify descriptions list", "an 'oai-identifier' element" );
		checker = checker.checkContainsOne( new Predicate<DescriptionType>() {

			@Override
			public boolean test( final DescriptionType description ) {
				final Object obj = description.getAny();
				if ( obj instanceof Element ) {
					final Element el = (Element) obj;
					if ( "Service".equals( el.getLocalName() ) && OPENAIRE_CERIF_XMLNS.equals( el.getNamespaceURI() ) ) {
						serviceAcronym = XmlUtils.getTextContents( XmlUtils.getFirstMatchingChild( el, "Acronym", el.getNamespaceURI() ) );
						return true;
					}
				}
				return false;
			}
			
		}, "the Identify descriptions list", "a 'Service' element" );
		checker.run();
		if ( ! endpoint.getBaseUrl().startsWith( "file:" ) ) {
			assertEquals( "Identify response has a different endpoint base URL", endpoint.getBaseUrl(), identify.getBaseURL() );
		}
		if ( serviceAcronym.isPresent() && repoIdentifier.isPresent() ) {
			assertEquals( "Service acronym is not the same as the repository identifier", serviceAcronym.get(), repoIdentifier.get() );
		}
	}
	
	@Test
	public void check010_MetadataFormats() throws Exception {
		CheckingIterable<MetadataFormatType> checker = CheckingIterable.over( endpoint.callListMetadataFormats().getMetadataFormat() );
		checker = checker.checkUnique( MetadataFormatType::getMetadataPrefix, "Metadata prefix not unique" );
		checker = checker.checkUnique( MetadataFormatType::getMetadataNamespace, "Metadata namespace not unique" );
		checker = checker.checkUnique( MetadataFormatType::getSchema, "Metadata schema location not unique" );
		checker = wrapCheckMetadataFormatPresent( checker, OAI_CERIF_OPENAIRE__METADATA_PREFIX, "https://www.openaire.eu/cerif-profile/1.1/" );
		checker.run();
	}
	
	private CheckingIterable<MetadataFormatType> wrapCheckMetadataFormatPresent( final CheckingIterable<MetadataFormatType> parent, final String expectedMetadataFormatPrefix, final String expectedMetadataFormatNamespace ) {
		final Predicate<MetadataFormatType> predicate = new Predicate<MetadataFormatType>() {

			@Override
			public boolean test( final MetadataFormatType mf ) {
				if ( expectedMetadataFormatPrefix.equals( mf.getMetadataPrefix() ) ) {
					assertEquals( "Non-matching set name for set '" + expectedMetadataFormatPrefix + "'", expectedMetadataFormatNamespace, mf.getMetadataNamespace() );
					// TODO check the schema can be read & has the right target namespace
					return true;
				}
				return false;
			}

		};
		return parent.checkContains( predicate, new AssertionError( "MetadataFormat '" + expectedMetadataFormatPrefix + "' not present" ) );
	}
	
	@Test
	public void check020_Sets() throws Exception {
		CheckingIterable<SetType> checker = CheckingIterable.over( endpoint.callListSets() );
		checker = checker.checkUnique( SetType::getSetSpec, "setSpec not unique" );
		checker = wrapCheckSetPresent( checker, OPENAIRE_CRIS_PUBLICATIONS__SET_SPEC, "OpenAIRE_CRIS_publications" );
		checker = wrapCheckSetPresent( checker, OPENAIRE_CRIS_PRODUCTS__SET_SPEC, "OpenAIRE_CRIS_products" );
		checker = wrapCheckSetPresent( checker, OPENAIRE_CRIS_PATENTS__SET_SPEC, "OpenAIRE_CRIS_patents" );
		checker = wrapCheckSetPresent( checker, OPENAIRE_CRIS_PERSONS__SET_SPEC, "OpenAIRE_CRIS_persons" );
		checker = wrapCheckSetPresent( checker, OPENAIRE_CRIS_ORGUNITS__SET_SPEC, "OpenAIRE_CRIS_orgunits" );
		checker = wrapCheckSetPresent( checker, OPENAIRE_CRIS_PROJECTS__SET_SPEC, "OpenAIRE_CRIS_projects" );
		checker = wrapCheckSetPresent( checker, OPENAIRE_CRIS_FUNDING__SET_SPEC, "OpenAIRE_CRIS_funding" );
		checker = wrapCheckSetPresent( checker, OPENAIRE_CRIS_EVENTS__SET_SPEC, "OpenAIRE_CRIS_events" );
		checker = wrapCheckSetPresent( checker, OPENAIRE_CRIS_EQUIPMENTS__SET_SPEC, "OpenAIRE_CRIS_equipments" );
		checker.run();
	}
	
	private CheckingIterable<SetType> wrapCheckSetPresent( final CheckingIterable<SetType> parent, final String expectedSetSpec, final String expectedSetName ) {
		final Predicate<SetType> predicate = new Predicate<SetType>() {

			@Override
			public boolean test( final SetType s ) {
				if ( expectedSetSpec.equals( s.getSetSpec() ) ) {
					assertEquals( "Non-matching set name for set '" + expectedSetSpec + "'", expectedSetName, s.getSetName() );
					return true;
				}
				return false;
			}

		};
		return parent.checkContains( predicate, new AssertionError( "Set '" + expectedSetSpec + "' not present" ) );
	}

	@Test
	public void check100_CheckPublications() throws Exception {
		final Iterable<RecordType> records = endpoint.callListRecords( OAI_CERIF_OPENAIRE__METADATA_PREFIX, OPENAIRE_CRIS_PUBLICATIONS__SET_SPEC, null, null );
		final CheckingIterable<RecordType> checker = buildCommonCheckersChain( records );
		checker.run();
	}

	@Test
	public void check200_CheckProducts() throws Exception {
		final Iterable<RecordType> records = endpoint.callListRecords( OAI_CERIF_OPENAIRE__METADATA_PREFIX, OPENAIRE_CRIS_PRODUCTS__SET_SPEC, null, null );
		final CheckingIterable<RecordType> checker = buildCommonCheckersChain( records );
		checker.run();
	}

	@Test
	public void check300_CheckPatents() throws Exception {
		final Iterable<RecordType> records = endpoint.callListRecords( OAI_CERIF_OPENAIRE__METADATA_PREFIX, OPENAIRE_CRIS_PATENTS__SET_SPEC, null, null );
		final CheckingIterable<RecordType> checker = buildCommonCheckersChain( records );
		checker.run();
	}

	@Test
	public void check400_CheckPersons() throws Exception {
		final Iterable<RecordType> records = endpoint.callListRecords( OAI_CERIF_OPENAIRE__METADATA_PREFIX, OPENAIRE_CRIS_PERSONS__SET_SPEC, null, null );
		final CheckingIterable<RecordType> checker = buildCommonCheckersChain( records );
		checker.run();
	}

	@Test
	public void check500_CheckOrgUnits() throws Exception {
		final Iterable<RecordType> records = endpoint.callListRecords( OAI_CERIF_OPENAIRE__METADATA_PREFIX, OPENAIRE_CRIS_ORGUNITS__SET_SPEC, null, null );
		final CheckingIterable<RecordType> checker = buildCommonCheckersChain( records );
		checker.run();
	}

	@Test
	public void check600_CheckProjects() throws Exception {
		final Iterable<RecordType> records = endpoint.callListRecords( OAI_CERIF_OPENAIRE__METADATA_PREFIX, OPENAIRE_CRIS_PROJECTS__SET_SPEC, null, null );
		final CheckingIterable<RecordType> checker = buildCommonCheckersChain( records );
		checker.run();
	}

	@Test
	public void check700_CheckFundings() throws Exception {
		final Iterable<RecordType> records = endpoint.callListRecords( OAI_CERIF_OPENAIRE__METADATA_PREFIX, OPENAIRE_CRIS_FUNDING__SET_SPEC, null, null );
		final CheckingIterable<RecordType> checker = buildCommonCheckersChain( records );
		checker.run();
	}

	@Test
	public void check800_CheckEquipment() throws Exception {
		final Iterable<RecordType> records = endpoint.callListRecords( OAI_CERIF_OPENAIRE__METADATA_PREFIX, OPENAIRE_CRIS_EQUIPMENTS__SET_SPEC, null, null );
		final CheckingIterable<RecordType> checker = buildCommonCheckersChain( records );
		checker.run();
	}

	@Test
	public void check900_CheckEvents() throws Exception {
		final Iterable<RecordType> records = endpoint.callListRecords( OAI_CERIF_OPENAIRE__METADATA_PREFIX, OPENAIRE_CRIS_EVENTS__SET_SPEC, null, null );
		final CheckingIterable<RecordType> checker = buildCommonCheckersChain( records );
		checker.run();
	}

	/**
	 * Prepare the checks to run on all CERIF records. 
	 * @param records
	 * @return
	 */
	protected CheckingIterable<RecordType> buildCommonCheckersChain( final Iterable<RecordType> records ) {
		return wrapCheckPayloadNamespaceAndAccummulate( wrapCheckUniqueness( wrapCheckOAIIdentifier( CheckingIterable.over( records ) ) ) );
	}
	
	private CheckingIterable<RecordType> wrapCheckUniqueness( final CheckingIterable<RecordType> checker ) {
		final Function<RecordType, HeaderType> f1 = RecordType::getHeader;
		return checker.checkUnique( f1.andThen( HeaderType::getIdentifier ), "record identifier not unique" );
	}
	
	private CheckingIterable<RecordType> wrapCheckOAIIdentifier( final CheckingIterable<RecordType> checker ) {
		if ( repoIdentifier.isPresent() ) {
			final Function<RecordType, String> expectedFunction = new Function<RecordType, String>() {
				
				@Override
				public String apply( final RecordType x ) {
					final MetadataType metadata = x.getMetadata();
					if ( metadata != null ) {
						final Element el = (Element) metadata.getAny();
						return "oai:" + repoIdentifier.get() + ":" + el.getLocalName() + "s/" + el.getAttribute( "id" );
					} else {
						// make the test trivially satisfied for records with no metadata
						return x.getHeader().getIdentifier();
					}
				}

			};
			return checker.checkForAllEquals( expectedFunction, ( final RecordType record ) -> ( record.getHeader().getIdentifier() ), "OAI identifier other than expected" );
		} else {
			return checker;
		}
	}

	private static Map<String, CERIFNode> recordsByName = new HashMap<>();
	
	private CheckingIterable<RecordType> wrapCheckPayloadNamespaceAndAccummulate( final CheckingIterable<RecordType> checker ) {
		return checker.checkForAll( new Predicate<RecordType>() {

			@Override
			public boolean test( final RecordType t ) {
				final MetadataType recordMetadata = t.getMetadata();
				if ( recordMetadata != null ) {
					final Object obj = recordMetadata.getAny();
					if ( obj instanceof Element ) {
						final Element el = (Element) obj;
						assertEquals( "The payload element not in the right namespace", OPENAIRE_CERIF_XMLNS, el.getNamespaceURI() );
						final CERIFNode node = CERIFNode.buildTree( el );
						recordsByName.put( node.getName(), node );
						return true;
					}
				}
				// fail unless the record is deleted
				return StatusType.DELETED.equals( t.getHeader().getStatus() );
			}
			
		}, "Metadata missing from OAI-PMH record" );
	}
	
	private static final String[] types = new String[] { "Publication", "Product", "Patent", "Person", "OrgUnit", "Project", "Funding", "Event", "Equipment" };
	static {
		Arrays.sort( types );
	}
	
	@Test
	public void check990_CheckFunctionalDependency() {
		for ( final CERIFNode node : recordsByName.values() ) {
			for ( final CERIFNode node3 : node.getChildren( null ) ) {
				lookForCERIFObjectsAndCheckFunctionalDependency( node3 );
			}
		}
	}

	private void lookForCERIFObjectsAndCheckFunctionalDependency( final CERIFNode node ) {
		final String type = node.getType();
		if ( Arrays.binarySearch( types, type ) >= 0 ) {
			doCheckFunctionalDependency( node );
		}
		for ( final CERIFNode node2 : node.getChildren( null ) ) {
			lookForCERIFObjectsAndCheckFunctionalDependency( node2 );
		}
	}

	private void doCheckFunctionalDependency( final CERIFNode node ) {
		final String name = node.getName();
		final CERIFNode baseNode = recordsByName.get( name );
		assertNotNull( "Record for " + name + " not found", baseNode );
		if ( ! node.isSubsetOf( baseNode ) ) {
			final CERIFNode missingNode = node.reportWhatIMiss( baseNode ).get();
			fail( node + "is not subset of\n" + baseNode + "missing is\n" + missingNode );
		}
	}
	
}
