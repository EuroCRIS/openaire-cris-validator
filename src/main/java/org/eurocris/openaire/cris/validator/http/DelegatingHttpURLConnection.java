package org.eurocris.openaire.cris.validator.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;

public class DelegatingHttpURLConnection extends DelegatingURLConnection {

	public DelegatingHttpURLConnection( final HttpURLConnection base ) {
		super( base );
		this.base = base;
	}

	protected final HttpURLConnection base;

	public void setFixedLengthStreamingMode( int contentLength ) {
		base.setFixedLengthStreamingMode( contentLength );
	}

	public void setFixedLengthStreamingMode( long contentLength ) {
		base.setFixedLengthStreamingMode( contentLength );
	}

	public void setChunkedStreamingMode( int chunklen ) {
		base.setChunkedStreamingMode( chunklen );
	}

	public void setInstanceFollowRedirects( boolean followRedirects ) {
		base.setInstanceFollowRedirects( followRedirects );
	}

	public boolean getInstanceFollowRedirects() {
		return base.getInstanceFollowRedirects();
	}

	public void setRequestMethod( String method ) throws ProtocolException {
		base.setRequestMethod( method );
	}

	public String getRequestMethod() {
		return base.getRequestMethod();
	}

	public int getResponseCode() throws IOException {
		return base.getResponseCode();
	}

	public String getResponseMessage() throws IOException {
		return base.getResponseMessage();
	}

	public InputStream getErrorStream() {
		return base.getErrorStream();
	}

	public boolean usingProxy() {
		return base.usingProxy();
	}

	public void disconnect() {
		base.disconnect();
	}

}
