package org.eurocris.openaire.cris.validator.util;

import java.util.Iterator;
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

	public static Iterable<Node> nodeListToIterableOfNodes( final NodeList nl ) {
		return nodeListToIterable( nl, Node.class );
	}
	
	public static Iterable<Element> nodeListToIterableOfElements( final NodeList nl ) {
		return nodeListToIterable( nl, Element.class );
	}
	
	private static <T extends Node> Iterable<T> nodeListToIterable( final NodeList nl, final Class<T> clazz ) {
		return new Iterable<T>() {

			@Override
			public Iterator<T> iterator() {
				return new Iterator<T>() {

					final int n = nl.getLength();
					int i = 0;
					
					@Override
					public boolean hasNext() {
						return ( i < n );
					}

					@SuppressWarnings( "unchecked")
					@Override
					public T next() {
						return (T) nl.item( i++ );
					}
					
				};
			}
			
		};
	}

}
