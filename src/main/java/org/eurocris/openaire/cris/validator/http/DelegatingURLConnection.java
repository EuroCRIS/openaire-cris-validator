package org.eurocris.openaire.cris.validator.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.Permission;
import java.util.List;
import java.util.Map;

public class DelegatingURLConnection extends URLConnection {

	public DelegatingURLConnection( final URLConnection base ) {
		super( base.getURL() );
		this.base = base;
	}

	protected final URLConnection base;

	public void connect() throws IOException {
		base.connect();
	}

	public void setConnectTimeout( int timeout ) {
		base.setConnectTimeout( timeout );
	}

	public int getConnectTimeout() {
		return base.getConnectTimeout();
	}

	public void setReadTimeout( int timeout ) {
		base.setReadTimeout( timeout );
	}

	public int getReadTimeout() {
		return base.getReadTimeout();
	}

	public URL getURL() {
		return base.getURL();
	}

	public int getContentLength() {
		return base.getContentLength();
	}

	public long getContentLengthLong() {
		return base.getContentLengthLong();
	}

	public String getContentType() {
		return base.getContentType();
	}

	public String getContentEncoding() {
		return base.getContentEncoding();
	}

	public long getExpiration() {
		return base.getExpiration();
	}

	public long getDate() {
		return base.getDate();
	}

	public long getLastModified() {
		return base.getLastModified();
	}

	public String getHeaderField( String name ) {
		return base.getHeaderField( name );
	}

	public Map<String, List<String>> getHeaderFields() {
		return base.getHeaderFields();
	}

	public int getHeaderFieldInt( String name, int Default ) {
		return base.getHeaderFieldInt( name, Default );
	}

	public long getHeaderFieldLong( String name, long Default ) {
		return base.getHeaderFieldLong( name, Default );
	}

	public long getHeaderFieldDate( String name, long Default ) {
		return base.getHeaderFieldDate( name, Default );
	}

	public String getHeaderFieldKey( int n ) {
		return base.getHeaderFieldKey( n );
	}

	public String getHeaderField( int n ) {
		return base.getHeaderField( n );
	}

	public Object getContent() throws IOException {
		throw new UnsupportedOperationException();
	}

	public Object getContent( @SuppressWarnings( "rawtypes") Class[] classes ) throws IOException {
		throw new UnsupportedOperationException();
	}

	public Permission getPermission() throws IOException {
		return base.getPermission();
	}

	public InputStream getInputStream() throws IOException {
		return base.getInputStream();
	}

	public OutputStream getOutputStream() throws IOException {
		return base.getOutputStream();
	}

	public void setDoInput( boolean doinput ) {
		base.setDoInput( doinput );
	}

	public boolean getDoInput() {
		return base.getDoInput();
	}

	public void setDoOutput( boolean dooutput ) {
		base.setDoOutput( dooutput );
	}

	public boolean getDoOutput() {
		return base.getDoOutput();
	}

	public void setAllowUserInteraction( boolean allowuserinteraction ) {
		base.setAllowUserInteraction( allowuserinteraction );
	}

	public boolean getAllowUserInteraction() {
		return base.getAllowUserInteraction();
	}

	public void setUseCaches( boolean usecaches ) {
		base.setUseCaches( usecaches );
	}

	public boolean getUseCaches() {
		return base.getUseCaches();
	}

	public void setIfModifiedSince( long ifmodifiedsince ) {
		base.setIfModifiedSince( ifmodifiedsince );
	}

	public long getIfModifiedSince() {
		return base.getIfModifiedSince();
	}

	public boolean getDefaultUseCaches() {
		return base.getDefaultUseCaches();
	}

	public void setDefaultUseCaches( boolean defaultusecaches ) {
		base.setDefaultUseCaches( defaultusecaches );
	}

	public void setRequestProperty( String key, String value ) {
		base.setRequestProperty( key, value );
	}

	public void addRequestProperty( String key, String value ) {
		base.addRequestProperty( key, value );
	}

	public String getRequestProperty( String key ) {
		return base.getRequestProperty( key );
	}

	public Map<String, List<String>> getRequestProperties() {
		return base.getRequestProperties();
	}
	
	public int hashCode() {
		return base.hashCode();
	}

	public boolean equals( Object obj ) {
		return ( obj instanceof DelegatingURLConnection ) 
			? base.equals( ( (DelegatingURLConnection) obj ).base ) 
			: base.equals( obj );
	}

	public String toString() {
		return getClass().getSimpleName() + ":" + base.toString();
	}

}
