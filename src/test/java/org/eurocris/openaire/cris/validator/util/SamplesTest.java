package org.eurocris.openaire.cris.validator.util;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.MissingArgumentException;
import org.eurocris.openaire.cris.validator.CRISValidator;
import org.xml.sax.SAXException;

/**
 * The test suite to test the OpenAIRE Guidelines 1.1 set of samples.
 */
public class SamplesTest extends CRISValidator {
	
	@SuppressWarnings("javadoc")
	public SamplesTest() throws MissingArgumentException, SAXException, IOException, ParserConfigurationException {
		super();
	}

	static {
		System.setProperty( "endpoint.to.validate", "file:samples/" );
	}

}
