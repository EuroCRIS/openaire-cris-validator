package org.eurocris.openaire.cris.validator.util;

import java.net.MalformedURLException;

import org.apache.commons.cli.MissingArgumentException;
import org.eurocris.openaire.cris.validator.CRISValidator;
import org.xml.sax.SAXException;

/**
 * The test suite to test the OpenAIRE Guidelines 1.1 set of samples.
 */
public class SamplesTest extends CRISValidator {
	
	@SuppressWarnings("javadoc")
	public SamplesTest() throws MalformedURLException, MissingArgumentException, SAXException {
		super();
	}

	static {
		System.setProperty( "endpoint.to.validate", "file:samples/" );
	}

}
