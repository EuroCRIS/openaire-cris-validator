package org.eurocris.openaire.cris.validator.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A {@link FilterInputStream} that copies the bytes it reads into a given {@link OutputStream}.
 */
public class TeeInputStream extends FilterInputStream {
	
	private final OutputStream out;
	
	public TeeInputStream( final InputStream in, final OutputStream out ) throws IOException {
		super( in );
		this.out = out;
	}

	public int read() throws IOException {
		final int b = super.read();
		if ( b != -1 ) {
			out.write( b );
		}
		return b;
	}

	public int read( byte[] b ) throws IOException {
		return read( b, 0, b.length );
	}

	public int read( byte[] b, int off, int len ) throws IOException {
		final int r = super.read( b, off, len );
		if ( r > 0 ) {
			out.write( b, off, r );
		}
		return r;
	}

	public boolean equals( Object obj ) {
		return super.equals( obj );
	}

	public int hashCode() {
		return 29 * super.hashCode() + out.hashCode();
	}

	public String toString() {
		return "TeeInputStream[ " + super.toString() + " -> " + out.toString() + " ]";
	}

	public void close() throws IOException {
		try {
			out.close();
		} finally {
			super.close();
		}
	}

	public boolean markSupported() {
		return false;
	}

	public void mark( int readlimit ) {
		throw new UnsupportedOperationException();
	}

	public void reset() throws IOException {
		throw new UnsupportedOperationException();
	}
	
}
