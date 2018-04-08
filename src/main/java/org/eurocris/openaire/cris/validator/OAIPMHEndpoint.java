package org.eurocris.openaire.cris.validator;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

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

	private final String baseUrl;

	private final String userAgent;

	private static final String URL_ENCODING = "UTF-8";

	public OAIPMHEndpoint( final URL endpointBaseUrl ) {
		this( endpointBaseUrl, OAIPMHtype.class.getName() );
	}

	public OAIPMHEndpoint( final URL endpointBaseUrl, final String userAgent ) {
		this.baseUrl = endpointBaseUrl.toExternalForm();
		this.userAgent = userAgent;
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
		final URLConnection conn = makeUrl( verb, params ).openConnection();
		conn.setRequestProperty( "User-Agent", userAgent );
		// TODO set other request headers if needed
		conn.connect();
		// TODO check the status and the response headers
		assert conn.getContentType().startsWith( "text/xml" );

		final Unmarshaller u = createUnmarshaller();
		final JAXBElement<OAIPMHtype> x = (JAXBElement<OAIPMHtype>) u.unmarshal( conn.getInputStream() );
		return x.getValue();
	}

	protected static Unmarshaller createUnmarshaller() throws JAXBException, SAXException {
		final JAXBContext jc = JAXBContext.newInstance( OAIPMHtype.class.getPackage().getName() );
		final SchemaFactory sf = SchemaFactory.newInstance( W3C_XML_SCHEMA_NS_URI );
		final Source[] schemas = { schema( "/openaire-cerif-profile.xsd" ), schema( "/cached/oai-identifier.xsd" ), schema( "/cached/OAI-PMH.xsd" ), schema( "/cached/oai_dc.xsd" ), };
		final Schema schema = sf.newSchema( schemas );
		final Unmarshaller u = jc.createUnmarshaller();
		u.setSchema( schema );
		return u;
	}

	private static Source schema( final String path ) {
		final String path1 = "/schemas" + path;
		final URL url = OAIPMHEndpoint.class.getResource( path1 );
		if ( url == null ) {
			throw new IllegalArgumentException( "Resource " + path1 + " not found" );
		}
		final StreamSource src = new StreamSource();
		src.setInputStream( OAIPMHEndpoint.class.getResourceAsStream( path1 ) );
		src.setSystemId( url.toExternalForm() );
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
	 * @param params
	 *            the parameters of the query
	 * @return a virtual collection that will use OAI-PMH resumption tokens to
	 *         get further elements; iterable just once
	 */
	public Iterable<RecordType> callListRecords( final String... params ) {
		return new ResumptionTokenIterable<RecordType, ListRecordsType>( "ListRecords", params, OAIPMHtype::getListRecords, ListRecordsType::getRecord, ListRecordsType::getResumptionToken );
	}

	/**
	 * Sends the ListIdentfiers request and returns the result. Repeats until
	 * all identifiers have been listed.
	 * 
	 * @param params
	 *            the parameters of the query
	 * @return a virtual collection that will use OAI-PMH resumption tokens to
	 *         get further elements; iterable just once
	 */
	public Iterable<HeaderType> callListIdentifiers( final String... params ) {
		return new ResumptionTokenIterable<HeaderType, ListIdentifiersType>( "ListIdentifiers", params, OAIPMHtype::getListIdentifiers, ListIdentifiersType::getHeader, ListIdentifiersType::getResumptionToken );
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