package org.eurocris.openaire.cris.validator.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FileSavingInputStream extends TeeInputStream {

	public FileSavingInputStream( final InputStream in, final Path path ) throws IOException {
		super( in, Files.newOutputStream( path, StandardOpenOption.CREATE_NEW ) );
	}

}
