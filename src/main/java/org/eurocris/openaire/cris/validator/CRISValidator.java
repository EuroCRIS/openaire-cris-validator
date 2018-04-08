package org.eurocris.openaire.cris.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.commons.cli.MissingArgumentException;
import org.eurocris.openaire.cris.validator.util.CheckingIterable;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runners.MethodSorters;
import org.openarchives.oai._2.DescriptionType;
import org.openarchives.oai._2.IdentifyType;
import org.openarchives.oai._2.MetadataFormatType;
import org.openarchives.oai._2.SetType;
import org.openarchives.oai._2_0.oai_identifier.OaiIdentifierType;

import junit.framework.AssertionFailedError;

@FixMethodOrder( value=MethodSorters.NAME_ASCENDING )
public class CRISValidator {
	
	public static void main( final String[] args ) throws Exception {
		final String endpointUrl = ( args.length > 0 ) ? args[0] : null;
		final URL endpointBaseUrl = new URL( endpointUrl );
		endpoint = new OAIPMHEndpoint( endpointBaseUrl );
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
			endpoint = new OAIPMHEndpoint( endpointBaseUrl );			
		}
	}
	
	public String getName() {
		return endpoint.getBaseUrl();
	}
	
	private Optional<String> sampleIdentifier = Optional.empty();
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
		checker = checkMetadataFormatPresent( checker, "oai_cerif_openaire", "https://www.openaire.eu/cerif-profile/1.1/" );
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
		return parent.checkContains( predicate, new AssertionFailedError( "MetadataFormat '" + expectedMetadataFormatPrefix + "' not present" ) );
	}
	
	@Test
	public void check020_Sets() throws Exception {
		CheckingIterable<SetType> checker = CheckingIterable.over( endpoint.callListSets() );
		checker = checkSetPresent( checker, "openaire_cris_publications", "OpenAIRE_CRIS_publications" );
		checker = checkSetPresent( checker, "openaire_cris_products", "OpenAIRE_CRIS_products" );
		checker = checkSetPresent( checker, "openaire_cris_patents", "OpenAIRE_CRIS_patents" );
		checker = checkSetPresent( checker, "openaire_cris_persons", "OpenAIRE_CRIS_persons" );
		checker = checkSetPresent( checker, "openaire_cris_orgunits", "OpenAIRE_CRIS_orgunits" );
		checker = checkSetPresent( checker, "openaire_cris_projects", "OpenAIRE_CRIS_projects" );
		checker = checkSetPresent( checker, "openaire_cris_funding", "OpenAIRE_CRIS_funding" );
		checker = checkSetPresent( checker, "openaire_cris_events", "OpenAIRE_CRIS_events" );
		checker = checkSetPresent( checker, "openaire_cris_equipments", "OpenAIRE_CRIS_equipments" );
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
		return parent.checkContains( predicate, new AssertionFailedError( "Set '" + expectedSetSpec + "' not present" ) );
	}

}
