package org.eurocris.openaire.cris.validator.util;

import java.io.IOException;
import java.net.URI;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.MissingArgumentException;
import org.eurocris.openaire.cris.validator.CRISValidator;
import org.xml.sax.SAXException;

/**
 * The test suite to test the current OpenAIRE CRIS Guidelines set of examples.
 */
public class SamplesTest extends CRISValidator {
	
	@SuppressWarnings("javadoc")
	public SamplesTest() throws MissingArgumentException, SAXException, IOException, ParserConfigurationException {
		super( URI.create( "file:samples/" ).toURL() );
	}

}
