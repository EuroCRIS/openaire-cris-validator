package org.eurocris.openaire.cris.validator.metadataTests;

import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * Adapting {@link org.eurocris.openaire.cris.validator.CRISValidator} for usage in this package.
 * Just inheriting the protected constructor is needed.
 */
public class CRISValidator extends org.eurocris.openaire.cris.validator.CRISValidator {

	/**
	 * A simple call to {@link org.eurocris.openaire.cris.validator.CRISValidator#CRISValidator( URL )}.
	 * @param endpointBaseUrl
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	protected CRISValidator(URL endpointBaseUrl) throws SAXException, IOException, ParserConfigurationException {
		super( endpointBaseUrl );
	}

}
