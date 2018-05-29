package org.eurocris.openaire.cris.validator.util;

import java.util.Iterator;
import java.util.Optional;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Useful operations that modernize the W3C DOM interface for XML. 
 */
public class XmlUtils {
	
	/**
	 * Get the first child element that matches the given localname and namespace.
	 * @param el the parent element
	 * @param localName the local name to match ("*" selects any local name)
	 * @param nsUri the namespace URI to match ("*" selects any namespace)
	 * @return the element, empty if no matching element is found
	 */
	public static Optional<Element> getFirstMatchingChild( final Element el, final String localName, final String nsUri ) {
		return Utils.getFirstElement( Utils.filter( nodeListToIterableOfElements( el.getElementsByTagNameNS( nsUri, localName ) ), ( final Element el1 ) -> el1.getParentNode() == el ) );
	}
	
	/**
	 * Get the first node of a {@link NodeList}.
	 * @param nl the given nodelist
	 * @return the first element; {@link Optional#empty()} if the nodelist was empty
	 */
	public static Optional<Node> nodeListToOptionalNode( final NodeList nl ) {
		return Optional.ofNullable( ( nl.getLength() > 0 ) ? nl.item( 0 ) : null );
	}

	/**
	 * Get the first element of a {@link NodeList}.
	 * @param nl the given nodelist, should contain {@link Element}s only
	 * @return the first element; {@link Optional#empty()} if the nodelist was empty
	 */
	public static Optional<Element> nodeListToOptionalElement( final NodeList nl ) {
		return Optional.ofNullable( ( nl.getLength() > 0 ) ? (Element) nl.item( 0 ) : null );
	}

	/**
	 * Get the (optional) text contents of an optional node.
	 * @param n the node
	 * @return the text contents
	 */
	public static Optional<String> getTextContents( final Optional<? extends Node> n ) {
		return n.map( Node::getTextContent );
	}

	/**
	 * Expose a {@link NodeList} as an {@link Iterable} of {@link Node}s.
	 * @param nl the nodelist
	 * @return the iterable
	 */
	public static Iterable<Node> nodeListToIterableOfNodes( final NodeList nl ) {
		return nodeListToIterable( nl, Node.class );
	}
	
	/**
	 * Expose a {@link NodeList} as an {@link Iterable} of {@link Element}s.
	 * @param nl the nodelist; may only contain {@link Element}s
	 * @return the iterable
	 */
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
