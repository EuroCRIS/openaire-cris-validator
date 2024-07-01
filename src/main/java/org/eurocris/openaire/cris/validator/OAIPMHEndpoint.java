package org.eurocris.openaire.cris.validator;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;

import org.eurocris.openaire.cris.validator.http.CompressionHandlingHttpURLConnectionAdapter;
import org.openarchives.oai._2.DescriptionType;
import org.openarchives.oai._2.HeaderType;
import org.openarchives.oai._2.IdentifyType;
import org.openarchives.oai._2.ListIdentifiersType;
import org.openarchives.oai._2.ListMetadataFormatsType;
import org.openarchives.oai._2.ListRecordsType;
import org.openarchives.oai._2.ListSetsType;
import org.openarchives.oai._2.OAIPMHerrorType;
import org.openarchives.oai._2.OAIPMHerrorcodeType;
import org.openarchives.oai._2.OAIPMHtype;
import org.openarchives.oai._2.RecordType;
import org.openarchives.oai._2.ResumptionTokenType;
import org.openarchives.oai._2.SetType;
import org.openarchives.oai._2_0.oai_identifier.OaiIdentifierType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * An OAI-PMH 2.0 endpoint client.
 * 
 * @author jdvorak
 */
public class OAIPMHEndpoint {

	private static final Logger logger = LoggerFactory.getLogger(OAIPMHEndpoint.class);
	
	private final String baseUrl;

	private final Schema schema;
	
	private final String userAgent;

	/**
	 * The way to make an {@link InputStream} from a connected {@link URLConnection}.
	 * @author jdvorak
	 */
	public static interface ConnectionStreamFactory {
		/**
		 * Make an {@link InputStream} from a connected {@link URLConnection}.
		 * @param conn the connection to start from, should be connected 
		 * @return the {@link InputStream} to be read
		 * @throws IOException
		 */
		InputStream makeInputStream( final URLConnection conn ) throws IOException;
	}
	
	private final ConnectionStreamFactory connStreamFactory;
	
	private Optional<List<String>> supportedCompressions = Optional.empty();
	
	private Optional<String> repositoryIdentifier = null;

	private static final String URL_ENCODING = "UTF-8";
	
	/**
	 * New endpoint client.
	 * @param endpointBaseUrl the base URL of the endpoint
	 * @param schema the compound schema for the responses: should include both the OAI-PMH schema and any schemas for the payload
	 * @param connStreamFactory the way to make an {@link InputStream} from a connected {@link URLConnection}
	 */
	public OAIPMHEndpoint( final URL endpointBaseUrl, final Schema schema, final ConnectionStreamFactory connStreamFactory ) {
		this( endpointBaseUrl, schema, connStreamFactory, OAIPMHtype.class.getName() );
	}

	/**
	 * New endpoint client.
	 * @param endpointBaseUrl the base URL of the endpoint
	 * @param schema the compound schema for the responses: should include both the OAI-PMH schema and any schemas for the payload
	 * @param connStreamFactory the way to make an {@link InputStream} from a connected {@link URLConnection}
	 * @param userAgent the designation of the client program to send in the 'User-Agent' HTTP header
	 */
	public OAIPMHEndpoint( final URL endpointBaseUrl, final Schema schema, final ConnectionStreamFactory connStreamFactory, final String userAgent ) {
		this.baseUrl = endpointBaseUrl.toExternalForm();
		this.userAgent = userAgent;
		this.schema = schema;
		this.connStreamFactory = connStreamFactory;
	}

	/**
	 * Get the base URL of the endpoint.
	 * @return the base URL
	 */
	public String getBaseUrl() {
		return baseUrl;
	}
	
	/**
	 * Get the repository identifier (after {@link #callIdentify()} had been called).
	 * @return the repository identifier if one was found in the response to the first Identify request
	 * @throws IllegalStateException if {@link #callIdentify()} had not been called yet
	 */
	public Optional<String> getRepositoryIdentifer() {
		if ( repositoryIdentifier == null ) {
			throw new IllegalStateException( "Repository identifier can only be available after callIdentify() had been called" );
		}
		return repositoryIdentifier;
	}

	/**
	 * Sends the Identify request and returns the result.
	 * @return the result of the Identify call
	 * @throws IOException on network error
	 * @throws SAXException on XML parsing error
	 * @throws JAXBException on XML processing error
	 */
	public IdentifyType callIdentify() throws IOException, SAXException, JAXBException {
		final IdentifyType identifyResponse = makeConnection( true, "Identify" ).getIdentify();
		supportedCompressions = Optional.of( identifyResponse.getCompression() );
		repositoryIdentifier = extractRepoIdentifier( identifyResponse );
		return identifyResponse;
	}

	/**
	 * Sends the ListMetadataFormats request and returns the result.
	 * @return the result of the ListMetadataFormats call
	 * @throws IOException on network error
	 * @throws SAXException on XML parsing error
	 * @throws JAXBException on XML processing error
	 */
	public ListMetadataFormatsType callListMetadataFormats() throws IOException, SAXException, JAXBException {
		return makeConnection( true, "ListMetadataFormats" ).getListMetadataFormats();
	}

	/**
	 * Sends the ListSets request and returns the result. 
	 * The returned {@link Iterable} will keep requesting information from the data provider using <code>resumptionToken</code>s until all sets are listed.
	 * @return a virtual collection that will use OAI-PMH resumption tokens to
	 *         get further elements; iterable just once
	 */
	public Iterable<SetType> callListSets() {
		return new ResumptionTokenIterable<SetType, ListSetsType>( true, "ListSets", new String[0], OAIPMHtype::getListSets, ListSetsType::getSet, ListSetsType::getResumptionToken );
	}

	/**
	 * Sends the ListRecords request and returns the result. 
	 * The returned {@link Iterable} will keep requesting information from the data provider using <code>resumptionToken</code>s until all records are listed.
	 * @param metadataFormatPrefix only fetch records in this metadata format (mandatory)
	 * @param setSpec only fetch records from this set (optional)
	 * @param from only fetch records at least this young (optional)
	 * @param until only fetch records older than this (optional)
	 * @return a virtual collection that will use OAI-PMH resumption tokens to
	 *         get further elements; iterable just once
	 */
	public Iterable<RecordType> callListRecords( final String metadataFormatPrefix, final String setSpec, final ZonedDateTime from, final ZonedDateTime until ) {
		final String[] params = collectHarvestingParameters( metadataFormatPrefix, setSpec, from, until );
		return new ResumptionTokenIterable<RecordType, ListRecordsType>( false, "ListRecords", params, OAIPMHtype::getListRecords, ListRecordsType::getRecord, ListRecordsType::getResumptionToken );
	}

	/**
	 * Sends the ListIdentfiers request and returns the result. 
	 * The returned {@link Iterable} will keep requesting information from the data provider using <code>resumptionToken</code>s until all identifiers are listed.
	 * @param metadataFormatPrefix only fetch records in this metadata format (mandatory)
	 * @param setSpec only fetch records from this set (optional)
	 * @param from only fetch records at least this young (optional)
	 * @param until only fetch records older than this (optional)
	 * @return a virtual collection that will use OAI-PMH resumption tokens to
	 *         get further elements; iterable just once
	 */
	public Iterable<HeaderType> callListIdentifiers( final String metadataFormatPrefix, final String setSpec, final ZonedDateTime from, final ZonedDateTime until ) {
		final String[] params = collectHarvestingParameters( metadataFormatPrefix, setSpec, from, until );
		return new ResumptionTokenIterable<HeaderType, ListIdentifiersType>( false, "ListIdentifiers", params, OAIPMHtype::getListIdentifiers, ListIdentifiersType::getHeader, ListIdentifiersType::getResumptionToken );
	}

	/**
	 * Contact the data provider with a request and return the parsed response.
	 * The response must be schema-valid.
	 * @param repoWideRequest true for Identify, ListMetadataFormats and ListSets
	 * @param verb the verb of the request
	 * @param params parameters of the request: pairs of ( name, value )
	 * @return the unmarshalled response
	 * @throws IOException on network error
	 * @throws SAXException on XML parsing error
	 * @throws JAXBException on XML processing error
	 */
	@SuppressWarnings( "unchecked")
	private OAIPMHtype makeConnection( final boolean repoWideRequest, final String verb, final String... params ) throws IOException, SAXException, JAXBException {
		final URL url = makeUrl( verb, params );
		logger.info( "Fetching and validating " + url.toExternalForm() );
		final URLConnection conn = handleCompression( url.openConnection() );
		conn.setRequestProperty( "User-Agent", userAgent );
		conn.setRequestProperty( "Accept", "text/xml, application/xml" );
		conn.connect();
		checkResponseCode( conn );
		checkContentTypeHeader( conn );
		checkContentEncodingHeader( conn );
		try ( final InputStream inputStream = connStreamFactory.makeInputStream( conn ) ) {
			final Unmarshaller u = createUnmarshaller();
			final JAXBElement<OAIPMHtype> x = (JAXBElement<OAIPMHtype>) u.unmarshal( inputStream );
			final OAIPMHtype response = x.getValue();
			checkForErrors( response );
			return response;
		}
	}

	private void checkResponseCode( final URLConnection conn ) throws IOException {
		if ( conn instanceof HttpURLConnection ) {
			final HttpURLConnection conn1 = (HttpURLConnection) conn;
			final int responseCode = conn1.getResponseCode();
			if ( responseCode != HttpURLConnection.HTTP_OK ) {
				throw new IllegalStateException( "Invalid response code " + responseCode + " " + conn1.getResponseMessage() );
			}
		}
	}

	private void checkContentEncodingHeader( final URLConnection conn ) {
		final String contentEncoding = conn.getContentEncoding();
		if ( ( contentEncoding != null ) && ! CompressionHandlingHttpURLConnectionAdapter.isIdentity( contentEncoding ) ) {
			throw new IllegalStateException( "The server returns a Content-Encoding we cannot handle: " + contentEncoding );
		}
	}

	private void checkContentTypeHeader( final URLConnection conn ) {
		// Although the OAI-PMH 2.0 spec, section 3.1.2.1, prescribes only "text/xml",
		// in the light of RFC7303 section 9.2 we accept "application/xml" as equivalent
		final String contentType = conn.getContentType();
		if (!( contentType.startsWith( "text/xml" ) || contentType.startsWith( "application/xml" ) )) {
			logger.error( "The Content-Type doesn't start with 'text/xml' or 'application/xml': " + contentType );
		}
	}

	private void checkForErrors( final OAIPMHtype response ) {
		final StringBuilder sb = new StringBuilder();
		for ( final OAIPMHerrorType error : response.getError() ) {
			if (! OAIPMHerrorcodeType.NO_RECORDS_MATCH.equals( error.getCode() ) ) {
				sb.append( error.getCode().name() );
				sb.append( ": " );
				sb.append( error.getValue() );
				sb.append( "; " );
			}
		}
		if ( sb.length() > 0 ) {
			final String errors = sb.substring( 0, sb.length() - 2 );
			throw new IllegalStateException( errors );
		}
	}

	/**
	 * Wraps the given {@link URLConnection} to ask for response compression and transparent decompression using one of the encodings the server said it supports (in its response to the Identify request).
	 * @param conn1 the connection to wrap
	 * @return the wrapped connection that transparently handles decompression
	 */
	protected URLConnection handleCompression( final URLConnection conn1 ) {
		final CompressionHandlingHttpURLConnectionAdapter conn = CompressionHandlingHttpURLConnectionAdapter.adapt( conn1 );
		if ( supportedCompressions.isPresent() ) {
			for ( final String compression : supportedCompressions.get() ) {
				conn.askForSupportedCompression( compression );
			}
		}
		return conn;
	}

	/**
	 * Creates the unmarshaller to use for de-serializing the OAI-PMH 2.0 responses and set the schema for validation (if one was given and validation should be done).
	 * @return the unmarshaller
	 * @throws JAXBException on problems initializing the unmarshaller
	 */
	protected Unmarshaller createUnmarshaller() throws JAXBException {
		final JAXBContext jc = JAXBContext.newInstance( OAIPMHtype.class, org.openarchives.oai._2_0.oai_identifier.ObjectFactory.class );
		final Unmarshaller u = jc.createUnmarshaller();
		if ( schema != null ) {
			u.setSchema( schema );
		}
		return u;
	}

	/**
	 * Extracts the OAI identifier's repository identifier value. 
	 * @param identifyResponse the response to an Identify request
	 * @param the repository identifier if one is provided, an empty {@link Optional} otherwise. 
	 */
	private Optional<String> extractRepoIdentifier( final IdentifyType identifyResponse ) {
		for ( final DescriptionType description : identifyResponse.getDescription() ) {
			final Object obj = description.getAny();
			if ( obj instanceof JAXBElement<?> ) {
				final JAXBElement<?> jaxbEl = (JAXBElement<?>) obj;
				final Object obj1 = jaxbEl.getValue();
				if ( obj1 instanceof OaiIdentifierType ) {
					final OaiIdentifierType oaiIdentifier = (OaiIdentifierType) obj1;
					return Optional.ofNullable( oaiIdentifier.getRepositoryIdentifier() );
				}
			}
		}
		return Optional.empty();
	}
	
	/**
	 * Constructs the parameters array to express the query.
	 * 
	 * @param metadataFormatPrefix only fetch records in this metadata format (mandatory)
	 * @param setSpec only fetch records from this set (optional)
	 * @param from only fetch records at least this young (optional)
	 * @param until only fetch records older than this (optional)
	 * @return the query as an array of keys and values (alternating)
	 */
	protected String[] collectHarvestingParameters( final String metadataFormatPrefix, final String setSpec, final ZonedDateTime from, final ZonedDateTime until ) {
		final List<String> params = new ArrayList<>();
		if ( metadataFormatPrefix != null ) {
			params.add( "metadataPrefix" );
			params.add( metadataFormatPrefix );
		} else {
			throw new NullPointerException( "The metadataFormat must be specified" );
		}
		if ( setSpec != null ) {
			params.add( "set" );
			params.add( setSpec );
		}
		final DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
		if ( from != null ) {
			params.add( "from" );
			params.add( from.format( formatter ) );
		}
		if ( until != null ) {
			params.add( "until" );
			params.add( until.format( formatter ) );
		}
		return params.toArray( new String[params.size()] );
	}

	private URL makeUrl( final String verb, final String... params ) throws UnsupportedEncodingException, MalformedURLException {
		final StringBuilder b = new StringBuilder( baseUrl );
		final boolean local = baseUrl.startsWith( "file:" );
		b.append( ( local ) ? '_' : '?' );
		b.append( "verb=" + verb );
		final char mainSep = ( local ) ? '+' : '&';
		char sep = mainSep;
		for ( final String param : params ) {
			b.append( sep );
			b.append( URLEncoder.encode( param, URL_ENCODING ) );
			sep = ( sep == '=' ) ? mainSep : '=';
		}
		if ( local ) {
			b.append( ".xml" );
		}
		return URI.create( b.toString() ).toURL();
	}

	/**
	 * The polymorphic Iterable over a cursor using a OAI-PMH resumption token.
	 * Iterable just once.
	 * 
	 * @author jdvorak
	 *
	 * @param <ItemType>
	 *            the type of the items
	 * @param <ListType>
	 *            the type that contains the chunks
	 */
	private class ResumptionTokenIterable<ItemType, ListType> implements Iterable<ItemType> {

		/**
		 * @param verb
		 *            the OAI-PMH verb
		 * @param params
		 *            the parameters of the OAI-PMH verb
		 * @param funcGetList
		 *            the function to get the type that contains the chunks
		 * @param functGetIterable
		 *            the function to get the iterable collection
		 * @param funcGetResumptionToken
		 *            the function to get the resumption token
		 */
		private ResumptionTokenIterable( final boolean repoWideRequest, final String verb, final String[] params, final Function<OAIPMHtype, ListType> funcGetList, final Function<ListType, Iterable<ItemType>> functGetIterable,
				final Function<ListType, ResumptionTokenType> funcGetResumptionToken ) {
			this.verb = verb;
			this.params = params;
			this.repoWideRequest = repoWideRequest;
			this.funcGetList = funcGetList;
			this.functGetIterable = functGetIterable;
			this.funcGetResumptionToken = funcGetResumptionToken;
		}

		private final String verb;
		private final String[] params;
		private boolean repoWideRequest;
		private final Function<OAIPMHtype, ListType> funcGetList;
		private final Function<ListType, Iterable<ItemType>> functGetIterable;
		private final Function<ListType, ResumptionTokenType> funcGetResumptionToken;

		private final AtomicReference<Iterator<ItemType>> iterator = new AtomicReference<>();

		/**
		 * The cursor-based iterator. Can be called just once, a second attempt
		 * raises an {@link IllegalStateException}. Any exceptions that arise
		 * while iterating will be re-thrown; non-RuntimeExceptions are wrapped
		 * in an {@link IllegalStateException}.
		 */
		@Override
		public Iterator<ItemType> iterator() {
			try {
				final Iterator<ItemType> i = new Iterator<ItemType>() {

					private ListType currentChunk = funcGetList.apply( makeConnection( repoWideRequest, verb, params ) );
					private Iterator<ItemType> innerIterator = ( currentChunk != null ) ? functGetIterable.apply( currentChunk ).iterator() : null;

					@Override
					public synchronized ItemType next() {
						if ( hasNext() ) {
							return innerIterator.next();
						}
						throw new NoSuchElementException();
					}

					@Override
					public synchronized boolean hasNext() {
						return ( innerIterator != null ) && ( innerIterator.hasNext() || advance() );
					}

					private boolean advance() {
						final ResumptionTokenType resumptionToken = funcGetResumptionToken.apply( currentChunk );
						if ( resumptionToken != null ) {
							final String resumptionTokenValue = resumptionToken.getValue();
							if ( ! resumptionTokenValue.isEmpty() ) {
								try {
									currentChunk = funcGetList.apply( makeConnection( repoWideRequest, verb, "resumptionToken", resumptionTokenValue ) );
									innerIterator = ( currentChunk != null ) ? functGetIterable.apply( currentChunk ).iterator() : null;
									return ( innerIterator != null );
								} catch ( final RuntimeException e ) {
									throw e;
								} catch ( final Throwable t ) {
									throw new IllegalStateException( t );
								}
							}
						}
						return false;
					}

				};
				if ( iterator.compareAndSet( null, i ) ) {
					return i;
				}
				throw new IllegalStateException( "Iterable just once" );
			} catch ( final RuntimeException e ) {
				throw e;
			} catch ( final Throwable t ) {
				throw new IllegalStateException( t );
			}
		}

	}

}