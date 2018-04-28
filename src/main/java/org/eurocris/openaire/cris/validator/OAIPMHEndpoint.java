package org.eurocris.openaire.cris.validator;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.eurocris.openaire.cris.validator.util.FileSavingInputStream;
import org.openarchives.oai._2.HeaderType;
import org.openarchives.oai._2.IdentifyType;
import org.openarchives.oai._2.ListIdentifiersType;
import org.openarchives.oai._2.ListMetadataFormatsType;
import org.openarchives.oai._2.ListRecordsType;
import org.openarchives.oai._2.ListSetsType;
import org.openarchives.oai._2.OAIPMHtype;
import org.openarchives.oai._2.RecordType;
import org.openarchives.oai._2.ResumptionTokenType;
import org.openarchives.oai._2.SetType;
import org.xml.sax.SAXException;

/**
 * An OAI-PMH 2.0 endpoint.
 * 
 * @author jdvorak
 */
public class OAIPMHEndpoint {
	
	private final String logDir;

	private final String baseUrl;

	private final String userAgent;

	private static final String URL_ENCODING = "UTF-8";
	
	private static final Pattern p1 = Pattern.compile( ".*\\W(set=\\w+).*" );

	public OAIPMHEndpoint( final URL endpointBaseUrl, final String logDir ) {
		this( endpointBaseUrl, logDir, OAIPMHtype.class.getName() );
	}

	public OAIPMHEndpoint( final URL endpointBaseUrl, final String logDir, final String userAgent ) {
		this.baseUrl = endpointBaseUrl.toExternalForm();
		this.userAgent = userAgent;
		this.logDir = logDir;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	private URL makeUrl( final String verb, final String... params ) throws UnsupportedEncodingException, MalformedURLException {
		final StringBuilder b = new StringBuilder( baseUrl );
		b.append( "?verb=" + verb );
		char sep = '&';
		for ( final String param : params ) {
			b.append( sep );
			b.append( URLEncoder.encode( param, URL_ENCODING ) );
			sep = ( sep == '&' ) ? '=' : '&';
		}
		return URI.create( b.toString() ).toURL();
	}

	@SuppressWarnings( "unchecked")
	private OAIPMHtype makeConnection( final String verb, final String... params ) throws IOException, JAXBException, SAXException {
		final URL url = makeUrl( verb, params );
		final URLConnection conn = url.openConnection();
		conn.setRequestProperty( "User-Agent", userAgent );
		// TODO set other request headers if needed
		conn.connect();
		// TODO check the status and the response headers
		assert conn.getContentType().startsWith( "text/xml" );

		final Unmarshaller u = createUnmarshaller();
		InputStream inputStream = conn.getInputStream();
		if ( logDir != null ) {
			final Path logDirPath = Paths.get( logDir );
			Files.createDirectories( logDirPath );
			final StringBuilder sb = new StringBuilder( verb );
			final Matcher m1 = p1.matcher( url.toExternalForm() );
 			if ( m1.matches() ) {
 				sb.append( "__" );
 				sb.append( m1.group( 1 ) );
 			}
			final DateTimeFormatter dtf = DateTimeFormatter.ofPattern( "yyyyMMdd'T'HHmmss.SSS" );
			final String logFilename = "oai-pmh--" + dtf.format( LocalDateTime.now() ) + "--" + sb.toString() + ".xml";
			inputStream = new FileSavingInputStream( inputStream, logDirPath.resolve( logFilename ) );
		}
		final JAXBElement<OAIPMHtype> x = (JAXBElement<OAIPMHtype>) u.unmarshal( inputStream );
		return x.getValue();
	}

	protected static Unmarshaller createUnmarshaller() throws JAXBException, SAXException {
		final JAXBContext jc = JAXBContext.newInstance( OAIPMHtype.class.getPackage().getName() );
		final Schema schema = createParserSchema();
		final Unmarshaller u = jc.createUnmarshaller();
		u.setSchema( schema );
		return u;
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

	/**
	 * Sends the Identify request and returns the result.
	 * 
	 * @return
	 * @throws IOException
	 * @throws JAXBException
	 * @throws SAXException
	 */
	public IdentifyType callIdentify() throws IOException, JAXBException, SAXException {
		return makeConnection( "Identify" ).getIdentify();
	}

	/**
	 * Sends the ListMetadataFormats request and returns the result.
	 * 
	 * @return
	 * @throws IOException
	 * @throws JAXBException
	 * @throws SAXException
	 */
	public ListMetadataFormatsType callListMetadataFormats() throws IOException, JAXBException, SAXException {
		return makeConnection( "ListMetadataFormats" ).getListMetadataFormats();
	}

	/**
	 * Sends the ListSets request and returns the result. Repeats until all sets
	 * have been listed.
	 * 
	 * @return a virtual collection that will use OAI-PMH resumption tokens to
	 *         get further elements; iterable just once
	 */
	public Iterable<SetType> callListSets() {
		return new ResumptionTokenIterable<SetType, ListSetsType>( "ListSets", new String[0], OAIPMHtype::getListSets, ListSetsType::getSet, ListSetsType::getResumptionToken );
	}

	/**
	 * Sends the ListRecords request and returns the result. Repeats until all
	 * records have been listed.
	 *
	 * @param metadataFormatPrefix only fetch records in this metadata format (mandatory)
	 * @param setSpec only fetch records from this set (optional)
	 * @param from only fetch records at least this young (optional)
	 * @param until only fetch records older than this (optional)
	 * @return a virtual collection that will use OAI-PMH resumption tokens to
	 *         get further elements; iterable just once
	 */
	public Iterable<RecordType> callListRecords( final String metadataFormatPrefix, final String setSpec, final ZonedDateTime from, final ZonedDateTime until ) {
		final String[] params = collectHarvestingParameters( metadataFormatPrefix, setSpec, from, until );
		return new ResumptionTokenIterable<RecordType, ListRecordsType>( "ListRecords", params, OAIPMHtype::getListRecords, ListRecordsType::getRecord, ListRecordsType::getResumptionToken );
	}

	/**
	 * Sends the ListIdentfiers request and returns the result. Repeats until
	 * all identifiers have been listed.
	 * 
	 * @param metadataFormatPrefix only fetch records in this metadata format (mandatory)
	 * @param setSpec only fetch records from this set (optional)
	 * @param from only fetch records at least this young (optional)
	 * @param until only fetch records older than this (optional)
	 * @return a virtual collection that will use OAI-PMH resumption tokens to
	 *         get further elements; iterable just once
	 */
	public Iterable<HeaderType> callListIdentifiers( final String metadataFormatPrefix, final String setSpec, final ZonedDateTime from, final ZonedDateTime until ) {
		final String[] params = collectHarvestingParameters( metadataFormatPrefix, setSpec, from, until );
		return new ResumptionTokenIterable<HeaderType, ListIdentifiersType>( "ListIdentifiers", params, OAIPMHtype::getListIdentifiers, ListIdentifiersType::getHeader, ListIdentifiersType::getResumptionToken );
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
		private ResumptionTokenIterable( final String verb, final String[] params, final Function<OAIPMHtype, ListType> funcGetList, final Function<ListType, Iterable<ItemType>> functGetIterable,
				final Function<ListType, ResumptionTokenType> funcGetResumptionToken ) {
			this.verb = verb;
			this.params = params;
			this.funcGetList = funcGetList;
			this.functGetIterable = functGetIterable;
			this.funcGetResumptionToken = funcGetResumptionToken;
		}

		private final String verb;
		private final String[] params;
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

					private ListType currentChunk = funcGetList.apply( makeConnection( verb, params ) );
					private Iterator<ItemType> innerIterator = functGetIterable.apply( currentChunk ).iterator();

					@Override
					public synchronized ItemType next() {
						if ( hasNext() ) {
							return innerIterator.next();
						}
						throw new NoSuchElementException();
					}

					@Override
					public synchronized boolean hasNext() {
						return innerIterator.hasNext() || advance();
					}

					private boolean advance() {
						final ResumptionTokenType resumptionToken = funcGetResumptionToken.apply( currentChunk );
						if ( resumptionToken != null ) {
							try {
								currentChunk = funcGetList.apply( makeConnection( verb, "resumptionToken", resumptionToken.getValue() ) );
								innerIterator = functGetIterable.apply( currentChunk ).iterator();
								return true;
							} catch ( final RuntimeException e ) {
								throw e;
							} catch ( final Throwable t ) {
								throw new IllegalStateException( t );
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