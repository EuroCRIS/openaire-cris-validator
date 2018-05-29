package org.eurocris.openaire.cris.validator.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * A {@link TeeInputStream} that saves the data in a file.
 */
public class FileSavingInputStream extends TeeInputStream {

	/**
	 * The new {@link TeeInputStream} over a given {@link InputStream} that saves the data in a given file.
	 * @param in the input stream
	 * @param path the location of the destination file; should not exist yet
	 * @throws IOException if something goes wrong
	 */
	public FileSavingInputStream( final InputStream in, final Path path ) throws IOException {
		super( in, Files.newOutputStream( path, StandardOpenOption.CREATE_NEW ) );
	}

}
