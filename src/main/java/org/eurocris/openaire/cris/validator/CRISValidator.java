package org.eurocris.openaire.cris.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.cli.MissingArgumentException;
import org.eurocris.openaire.cris.validator.util.CheckingIterable;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runners.MethodSorters;
import org.openarchives.oai._2.DescriptionType;
import org.openarchives.oai._2.HeaderType;
import org.openarchives.oai._2.IdentifyType;
import org.openarchives.oai._2.MetadataFormatType;
import org.openarchives.oai._2.RecordType;
import org.openarchives.oai._2.SetType;
import org.openarchives.oai._2_0.oai_identifier.OaiIdentifierType;

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
	
	public static final String LOG_DIR = "data";

	public static void main( final String[] args ) throws Exception {
		final String endpointUrl = ( args.length > 0 ) ? args[0] : null;
		final URL endpointBaseUrl = new URL( endpointUrl );
		endpoint = new OAIPMHEndpoint( endpointBaseUrl, LOG_DIR );
		JUnitCore.main( CRISValidator.class.getName() );
	}
	
	private static OAIPMHEndpoint endpoint;

	public CRISValidator() throws MalformedURLException, MissingArgumentException {
		if ( endpoint == null ) {
			final String endpointPropertyKey = "endpoint.to.validate";
			final String endpointUrl = System.getProperty( endpointPropertyKey );
			if ( endpointUrl == null ) {
				throw new MissingArgumentException( "Please specify the OAI-PMH endpoint URL as the value of the " + endpointPropertyKey + " system property or as the first argument on the command line" );
			}
			final URL endpointBaseUrl = new URL( endpointUrl );
			endpoint = new OAIPMHEndpoint( endpointBaseUrl, LOG_DIR );			
		}
	}
	
	public String getName() {
		return endpoint.getBaseUrl();
	}
	
	@SuppressWarnings( "unused")
	private Optional<String> sampleIdentifier = Optional.empty();
	@SuppressWarnings( "unused")
	private Optional<String> repoIdentifier = Optional.empty();
	
	@Test
	public void check000_Identify() throws Exception {
		final IdentifyType identify = endpoint.callIdentify();
		
		assertEquals( "Identify response has a different endpoint base URL", endpoint.getBaseUrl(), identify.getBaseURL() );
		
		final List<DescriptionType> descriptions = identify.getDescription();
		// boolean serviceSeen = false;
		boolean oaiIdentifierSeen = false;
		for ( final DescriptionType description : descriptions ) {
			final Object obj = description.getAny();

			if ( obj instanceof OaiIdentifierType ) {
				oaiIdentifierSeen = true;
				final OaiIdentifierType oaiIdentifier = (OaiIdentifierType) obj;
				sampleIdentifier = Optional.ofNullable( oaiIdentifier.getSampleIdentifier() );
				repoIdentifier = Optional.ofNullable( oaiIdentifier.getRepositoryIdentifier() );
			}

			// TODO
		}
		assertTrue( "No 'description' contains an 'oai-identifier' element", oaiIdentifierSeen );
		// assertTrue( "No 'description' contains a 'Service' element", serviceSeen );
	}
	
	@Test
	public void check010_MetadataFormats() throws Exception {
		CheckingIterable<MetadataFormatType> checker = CheckingIterable.over( endpoint.callListMetadataFormats().getMetadataFormat() );
		checker = checker.checkUnique( MetadataFormatType::getMetadataPrefix, "Metadata prefix not unique" );
		checker = checker.checkUnique( MetadataFormatType::getMetadataNamespace, "Metadata namespace not unique" );
		checker = checker.checkUnique( MetadataFormatType::getSchema, "Metadata schema location not unique" );
		checker = checkMetadataFormatPresent( checker, OAI_CERIF_OPENAIRE__METADATA_PREFIX, "https://www.openaire.eu/cerif-profile/1.1/" );
		checker.run();
	}
	
	private CheckingIterable<MetadataFormatType> checkMetadataFormatPresent( final CheckingIterable<MetadataFormatType> parent, final String expectedMetadataFormatPrefix, final String expectedMetadataFormatNamespace ) {
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
		checker = checkSetPresent( checker, OPENAIRE_CRIS_PUBLICATIONS__SET_SPEC, "OpenAIRE_CRIS_publications" );
		checker = checkSetPresent( checker, OPENAIRE_CRIS_PRODUCTS__SET_SPEC, "OpenAIRE_CRIS_products" );
		checker = checkSetPresent( checker, OPENAIRE_CRIS_PATENTS__SET_SPEC, "OpenAIRE_CRIS_patents" );
		checker = checkSetPresent( checker, OPENAIRE_CRIS_PERSONS__SET_SPEC, "OpenAIRE_CRIS_persons" );
		checker = checkSetPresent( checker, OPENAIRE_CRIS_ORGUNITS__SET_SPEC, "OpenAIRE_CRIS_orgunits" );
		checker = checkSetPresent( checker, OPENAIRE_CRIS_PROJECTS__SET_SPEC, "OpenAIRE_CRIS_projects" );
		checker = checkSetPresent( checker, OPENAIRE_CRIS_FUNDING__SET_SPEC, "OpenAIRE_CRIS_funding" );
		checker = checkSetPresent( checker, OPENAIRE_CRIS_EVENTS__SET_SPEC, "OpenAIRE_CRIS_events" );
		checker = checkSetPresent( checker, OPENAIRE_CRIS_EQUIPMENTS__SET_SPEC, "OpenAIRE_CRIS_equipments" );
		checker.run();
	}
	
	private CheckingIterable<SetType> checkSetPresent( final CheckingIterable<SetType> parent, final String expectedSetSpec, final String expectedSetName ) {
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
		CheckingIterable<RecordType> checker = uniquenessChecker( endpoint.callListRecords( OAI_CERIF_OPENAIRE__METADATA_PREFIX, OPENAIRE_CRIS_PUBLICATIONS__SET_SPEC, null, null ) );
		checker.run();
	}

	@Test
	public void check200_CheckProducts() throws Exception {
		CheckingIterable<RecordType> checker = uniquenessChecker( endpoint.callListRecords( OAI_CERIF_OPENAIRE__METADATA_PREFIX, OPENAIRE_CRIS_PRODUCTS__SET_SPEC, null, null ) );
		checker.run();
	}

	@Test
	public void check300_CheckPatents() throws Exception {
		CheckingIterable<RecordType> checker = uniquenessChecker( endpoint.callListRecords( OAI_CERIF_OPENAIRE__METADATA_PREFIX, OPENAIRE_CRIS_PATENTS__SET_SPEC, null, null ) );
		checker.run();
	}

	@Test
	public void check400_CheckPersons() throws Exception {
		CheckingIterable<RecordType> checker = uniquenessChecker( endpoint.callListRecords( OAI_CERIF_OPENAIRE__METADATA_PREFIX, OPENAIRE_CRIS_PERSONS__SET_SPEC, null, null ) );
		checker.run();
	}

	@Test
	public void check500_CheckOrgUnits() throws Exception {
		CheckingIterable<RecordType> checker = uniquenessChecker( endpoint.callListRecords( OAI_CERIF_OPENAIRE__METADATA_PREFIX, OPENAIRE_CRIS_ORGUNITS__SET_SPEC, null, null ) );
		checker.run();
	}

	@Test
	public void check600_CheckProjects() throws Exception {
		CheckingIterable<RecordType> checker = uniquenessChecker( endpoint.callListRecords( OAI_CERIF_OPENAIRE__METADATA_PREFIX, OPENAIRE_CRIS_PROJECTS__SET_SPEC, null, null ) );
		checker.run();
	}

	@Test
	public void check700_CheckFundings() throws Exception {
		CheckingIterable<RecordType> checker = uniquenessChecker( endpoint.callListRecords( OAI_CERIF_OPENAIRE__METADATA_PREFIX, OPENAIRE_CRIS_FUNDING__SET_SPEC, null, null ) );
		checker.run();
	}

	@Test
	public void check800_CheckEquipment() throws Exception {
		CheckingIterable<RecordType> checker = uniquenessChecker( endpoint.callListRecords( OAI_CERIF_OPENAIRE__METADATA_PREFIX, OPENAIRE_CRIS_EQUIPMENTS__SET_SPEC, null, null ) );
		checker.run();
	}

	@Test
	public void check900_CheckEvents() throws Exception {
		CheckingIterable<RecordType> checker = uniquenessChecker( endpoint.callListRecords( OAI_CERIF_OPENAIRE__METADATA_PREFIX, OPENAIRE_CRIS_EVENTS__SET_SPEC, null, null ) );
		checker.run();
	}

	private CheckingIterable<RecordType> uniquenessChecker( final Iterable<RecordType> records ) {
		final CheckingIterable<RecordType> checker = CheckingIterable.over( records );
		final Function<RecordType, HeaderType> f1 = RecordType::getHeader;
		return checker.checkUnique( f1.andThen( HeaderType::getIdentifier ), "record identifier not unique" );
	}
	
}
