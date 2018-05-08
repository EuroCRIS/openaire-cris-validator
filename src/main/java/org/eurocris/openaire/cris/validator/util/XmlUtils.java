package org.eurocris.openaire.cris.validator.util;

import java.util.Optional;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlUtils {
	
	public static Optional<Element> getFirstMatchingChild( final Element el, final String localName, final String nsUri ) {
		return nodeListToOptionalElement( el.getElementsByTagNameNS( nsUri, localName ) );
	}
	
	public static Optional<Node> nodeListToOptionalNode( final NodeList nl ) {
		return Optional.ofNullable( ( nl.getLength() > 0 ) ? nl.item( 0 ) : null );
	}

	public static Optional<Element> nodeListToOptionalElement( final NodeList nl ) {
		return Optional.ofNullable( ( nl.getLength() > 0 ) ? (Element) nl.item( 0 ) : null );
	}

	public static Optional<String> getTextContents( final Optional<? extends Node> n ) {
		return n.map( Node::getTextContent );
	}

}
