package org.eurocris.openaire.cris.validator.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;

/**
 * An adapter that transparently handles compression of the contents of HTTP responses.
 * @author jdvorak
 */
public class CompressionHandlingHttpURLConnectionAdapter extends DelegatingURLConnection {

	private static final String ACCEPT_ENCODING = "Accept-Encoding";
	
	private static final String CONTENT_ENCODING = "Content-Encoding";
	
	private static final String CONTENT_LENGTH = "Content-Length";
	
	private static final String IDENTITY = "identity";
	
	/**
	 * Adapt a base connection.
	 * @param base the connection to provide transparent compression handling for
	 * @return the adapted connection
	 */
	public static CompressionHandlingHttpURLConnectionAdapter adapt( final URLConnection base ) {
		return new CompressionHandlingHttpURLConnectionAdapter( base );
	}
	
	private CompressionHandlingHttpURLConnectionAdapter( final URLConnection base ) {
		super( base );
	}
	
	private boolean connectCalled = false;
	
	private final StringBuilder acceptEncodingHeaderBuilder = new StringBuilder();
	
	private static final Map<String, InputStreamCompressionAdapter> registeredAdapters = new HashMap<>();
	static {
		register( new GzipInputStreamConnectionAdapter( "gzip" ) );
		register( new GzipInputStreamConnectionAdapter( "x-gzip" ) );
		register( new DeflaterInputStreamConnectionAdapter( "deflate" ) );
		register( new DeflaterInputStreamConnectionAdapter( "x-deflate" ) );
	}

	/**
	 * Register a compression adapter. Will not register an adapter for the same name twice.
	 * By default this class knows "gzip" (a.k.a. "x-gzip") and "deflate" (a.k.a. "x-deflate") compressions.
	 * @param isca the adapter to register
	 * @return true if the adapter was registered; false if the encoding token was already occupied
	 */
	public static boolean register( final InputStreamCompressionAdapter isca ) {
		return ( registeredAdapters.putIfAbsent( isca.getEncodingToken(), isca ) == null );
	}
	
	/**
	 * Ask for a specific compression, even if it will not be handled by this adapter.
	 * @param compression the encoding token for the compression
	 * @return true if the compression will be supported by this adapter
	 * @throws IllegalStateException if {@link #connect()} has already been called
	 */
	public boolean askForAnyCompression( final String compression ) {
		if ( connectCalled ) {
			throw new IllegalStateException( "connect() was already called" );
		}
		acceptEncodingHeaderBuilder.append( ", " );
		acceptEncodingHeaderBuilder.append( compression );
		return isCompressionSupported( compression );
	}

	/**
	 * Ask for a specific compression if it will be handled by this adapter.
	 * @param compression the encoding token for the compression
	 * @return true if the compression will be supported by this adapter
	 * @throws IllegalStateException if {@link #connect()} has already been called
	 */
	public boolean askForSupportedCompression( final String compression ) {
		if ( connectCalled ) {
			throw new IllegalStateException( "connect() was already called" );
		}
		if ( isCompressionSupported( compression ) ) {
			acceptEncodingHeaderBuilder.append( ", " );
			acceptEncodingHeaderBuilder.append( compression );
			return true;
		}
		return false;
	}

	private boolean isCompressionSupported( final String compression ) {
		return isIdentity( compression ) || registeredAdapters.containsKey( compression );
	}

	/**
	 * Test if the given content encoding is identity.
	 * @param contentEncoding the content encoding to check
	 * @return true if the given content encoding is identity
	 */
	public static boolean isIdentity( final String contentEncoding ) {
		return IDENTITY.equals( contentEncoding );
	}
	
	/**
	 * Sets the "Accept-Encoding" request header and calls {@link URLConnection#connect()} on the base connection.
	 */
	@Override
	public void connect() throws IOException {
		connectCalled = true;
		fieldKeys = null;
		if ( acceptEncodingHeaderBuilder.length() > 0 ) {
			super.setRequestProperty( ACCEPT_ENCODING, acceptEncodingHeaderBuilder.substring( 2 ) );
		}
		super.connect();
	}

	/**
	 * Returns -1 if the response will be transparently uncompressed. 
	 */
	@Override
	public int getContentLength() {
		return ( isResponseCompressed() ) ? -1 : super.getContentLength();
	}

	/**
	 * Returns -1L if the response will be transparently uncompressed. 
	 */
	@Override
	public long getContentLengthLong() {
		return ( isResponseCompressed() ) ? -1L : super.getContentLengthLong();
	}

	/**
	 * Returns "identity" if the response will be transparently uncompressed. 
	 */
	@Override
	public String getContentEncoding() {
		final String contentEncoding = super.getContentEncoding();
		return ( isResponseCompressed() ) ? IDENTITY : contentEncoding;
	}

	/**
	 * The response is compressed with a registered compression.
	 * @return
	 */
	private boolean isResponseCompressed() {
		final String contentEncoding = super.getContentEncoding();
		return registeredAdapters.containsKey( contentEncoding );
	}
	
	/**
	 * Returns the original header value unless the response will be transparently uncompressed and you ask for "Content-Encoding" or "Content-Length".
	 */
	@Override
	public String getHeaderField( final String name ) {
		if ( isResponseCompressed() ) {
			if ( CONTENT_ENCODING.equals( name ) ) {
				return IDENTITY;
			} else if ( CONTENT_LENGTH.equals( name ) ) {
				return null;
			}
		}
		return super.getHeaderField( name );
	}

	@Override
	public Map<String, List<String>> getHeaderFields() {
		final boolean compressed = isResponseCompressed();
		final Map<String, List<String>> result = new HashMap<>();
		for ( final Map.Entry<String, List<String>> e : super.getHeaderFields().entrySet() ) {
			final String key = e.getKey();
			final List<String> value1 = e.getValue();
			List<String> value2 = value1;
			if ( compressed ) {
				if ( CONTENT_ENCODING.equals( key ) ) {
					value2 = Collections.singletonList( IDENTITY );
				} else if ( CONTENT_LENGTH.equals( key ) ) {
					continue;
				}	
			}
			result.put( key, value2 );
		}
		return Collections.unmodifiableMap( result );
	}

	@Override
	public int getHeaderFieldInt( final String name, final int Default ) {
		return ( CONTENT_LENGTH.equals( name ) && isResponseCompressed() ) ? Default : super.getHeaderFieldInt( name, Default );
	}

	@Override
	public long getHeaderFieldLong( final String name, final long Default ) {
		return ( CONTENT_LENGTH.equals( name ) && isResponseCompressed() ) ? Default : super.getHeaderFieldLong( name, Default );
	}

	@Override
	public long getHeaderFieldDate( final String name, final long Default ) {
		return super.getHeaderFieldDate( name, Default );
	}

	@Override
	public String getHeaderFieldKey( final int n ) {
		final Integer nn = convertFieldIndex( n );
		return ( nn != null ) ? super.getHeaderFieldKey( nn ) : null;
	}

	@Override
	public String getHeaderField( final int n ) {
		final Integer nn = convertFieldIndex( n );
		return ( nn != null ) 
			? (
				( isResponseCompressed() && CONTENT_ENCODING.equals( super.getHeaderFieldKey( nn ) ) ) ? IDENTITY : super.getHeaderField( nn ) 
			  ) : null;
	}

	private List<Integer> fieldKeys = null;
	
	private Integer convertFieldIndex( final int n ) {
		ensureHeaderFieldsIndexed();
		return ( n < fieldKeys.size() ) ? fieldKeys.get( n ) : null;
	}

	private void ensureHeaderFieldsIndexed() {
		if ( fieldKeys == null ) {
			fieldKeys = new ArrayList<>();
			int i = 0;
			String key;
			while ( ( key = super.getHeaderFieldKey( i ) ) != null ) {
				if (!( CONTENT_LENGTH.equals( key ) && isResponseCompressed() )) {
					fieldKeys.add( fieldKeys.size(), i );					
				}
				++i;
			}
		}
	}
	
	/**
	 * Gets the {@link InputStream} that reads from this open connection, handling known compressions transparently.
	 */
	public InputStream getInputStream() throws IOException {
		final InputStream is = super.getInputStream();
		final String contentEncoding = super.getContentEncoding();
		final InputStreamCompressionAdapter isca = registeredAdapters.get( contentEncoding );
		return ( isca != null ) ? isca.adapt( is ) : is;
	}

	public void setRequestProperty( String key, String value ) {
		checkNotAcceptEncoding( key );
		super.setRequestProperty( key, value );
	}

	public void addRequestProperty( String key, String value ) {
		checkNotAcceptEncoding( key );
		super.addRequestProperty( key, value );
	}

	private void checkNotAcceptEncoding( final String key ) {
		if ( ACCEPT_ENCODING.equals( key ) ) {
			throw new UnsupportedOperationException( "Please do not set the " + ACCEPT_ENCODING + " header directly, use the askForCompression() method instead" );
		}
	}

}

/**
 * {@link InputStream} wrapper maker class with a set token.
 */
abstract class InputStreamCompressionAdapter {
	
	/**
	 * The wrapper maker with the given token.
	 * @param encodingToken the token this adapter is known under
	 */
	public InputStreamCompressionAdapter( final String encodingToken ) {
		this.encodingToken = encodingToken;
	}

	private final String encodingToken;
	
	/**
	 * @return the token this adapter is known under
	 */
	public String getEncodingToken() {
		return encodingToken;
	}
	
	/**
	 * Wrap the given {@link InputStream}.
	 * @param in the {@link InputStream} to wrap
	 * @return the wrapped {@link InputStream}
	 * @throws IOException if there is a problem with the wrapping
	 */
	public abstract InputStream adapt( final InputStream in ) throws IOException;
	
}

/**
 * Wrapping with {@link GZIPInputStream}.
 */
class GzipInputStreamConnectionAdapter extends InputStreamCompressionAdapter {
	
	/**
	 * The {@link GZIPInputStream} wrapper maker with the given token.
	 * @param encodingToken the token this adapter is known under
	 */
	public GzipInputStreamConnectionAdapter( final String encodingToken ) {
		super( encodingToken );
	}

	@Override
	public InputStream adapt( final InputStream in ) throws IOException {
		return new GZIPInputStream( in );
	}
	
}

/**
 * Wrapping with {@link DeflaterInputStream}.
 */
class DeflaterInputStreamConnectionAdapter extends InputStreamCompressionAdapter {
	
	/**
	 * The {@link DeflaterInputStream} wrapper maker with the given token.
	 * @param encodingToken the token this adapter is known under
	 */
	public DeflaterInputStreamConnectionAdapter( final String encodingToken ) {
		super( encodingToken );
	}

	@Override
	public InputStream adapt( final InputStream in ) throws IOException {
		return new DeflaterInputStream( in );
	}
	
}