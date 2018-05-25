package org.eurocris.openaire.cris.validator.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.xml.XMLConstants;

import org.eurocris.openaire.cris.validator.util.XmlUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * A node to represent an element of CERIF XML.
 * @author jdvorak
 */
public class CERIFNode {
	
	private final String type;
	
	private final String name;
	
	private String value;
	
	private final Map<String, List<CERIFNode>> data = new HashMap<>();
	
	/**
	 * A new node from an {@link Element}.
	 * @param el the element to extract
	 */
	public CERIFNode( final Element el ) {
		final StringBuilder sb = new StringBuilder( el.getLocalName() );
		this.type = sb.toString().intern();
		final NamedNodeMap attributes = el.getAttributes();
		if ( attributes != null ) {
			final int n = attributes.getLength();
			for ( int i = 0; i < n; ++i ) {
				final Attr attr = (Attr) attributes.item( i );
				final String nsURI = attr.getNamespaceURI();
				if ( ! XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals( nsURI ) ) {
					sb.append( '[' );
					sb.append( '@' );
					if ( nsURI != null ) {
						sb.append( '{' );
						sb.append( nsURI );
						sb.append( '}' );
					}
					final String localName = attr.getLocalName();
					sb.append( localName );
					final String value = attr.getValue();
					sb.append( '=' );
					sb.append( '\"' );
					sb.append( value );
					sb.append( '\"' );
					sb.append( ']' );
				}
			}
		}
		this.name = sb.toString().intern();
		this.value = el.getTextContent().trim();
	}

	/**
	 * @return the complete name of the node
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return just the type of the node (the first part of the name)
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * @return the string value of the node (if a leaf node)
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Build the tree recursively from the given element.
	 * @param node the element to build from
	 * @return the built subtree
	 */
	public static CERIFNode buildTree( final Element node ) {
		final CERIFNode result = new CERIFNode( (Element) node );
		for ( final Node node2 : XmlUtils.nodeListToIterableOfNodes( node.getChildNodes() ) ) {
			if ( node2 instanceof Element ) {
				final CERIFNode x2 = CERIFNode.buildTree( (Element) node2 );
				result.addChild( x2 );
			}
		}
		return result;
	}
	
	@Override
	public String toString() {
		return toString( "- " );
	}
	
	/**
	 * Adds a subtree.
	 * @param node the subtree root to add
	 */
	public void addChild( final CERIFNode node ) {
		final String name = node.getName();
		final List<CERIFNode> newList = new ArrayList<>(1);
		final List<CERIFNode> oldList = data.putIfAbsent( name, newList );
		final List<CERIFNode> list = ( oldList != null ) ? oldList : newList;
		list.add( node );
		value = "";
	}
	
	/**
	 * The children of this element with a given name.
	 * @param name the name to look for; null selects all children
	 * @return the iterable of the children
	 */
	public Iterable<CERIFNode> getChildren( final String name ) {
		return new Iterable<CERIFNode>() {

			private final Collection<List<CERIFNode>> coll1 = ( name == null ) ? data.values() 
						: ( data.get( name ) != null ) ? Collections.singleton( data.get( name ) ) : Collections.emptyList();
			
			private final Iterator<List<CERIFNode>> it1 = coll1.iterator();
			
			@Override
			public Iterator<CERIFNode> iterator() {
				return new Iterator<CERIFNode>() {

					private Iterator<CERIFNode> it2 = ( it1.hasNext() ) ? it1.next().iterator() : null;
					
					@Override
					public boolean hasNext() {
						return ( it2 != null ) && ( it2.hasNext() || advance() );
					}

					private boolean advance() {
						while ( it1.hasNext() ) {
							it2 = it1.next().iterator();
							if ( it2.hasNext() ) {
								return true;
							}
						}
						return false;
					}

					@Override
					public CERIFNode next() {
						if ( hasNext() ) {
							return it2.next();
						}
						throw new NoSuchElementException();
					}
					
				};
			}
			
		};
	}

	/**
	 * Write out the subtree with a given indentation.
	 * @param indent the indentation to prepend to all lines of output
	 * @return the writeout
	 */
	public String toString( final String indent ) {
		final StringBuilder sb = new StringBuilder( indent + getName() + ": " + getValue() + "\n" );
		final String newIndent = "  " + indent;
		for ( final CERIFNode node : getChildren( null ) ) {
			sb.append( node.toString( newIndent ) );
		}
		return sb.toString();
	}
	
	/**
	 * Try to find my child that is a superset of the given node.
	 * @param what the node to look for
	 * @return the matching child, or empty
	 */
	public Optional<CERIFNode> lookup( final CERIFNode what ) {
		final String whatName = what.getName();
		final List<CERIFNode> candidates = data.get( whatName );
		if ( candidates != null ) {
			for ( final CERIFNode candidate : candidates ) {
				if ( what.isSubsetOf( candidate ) ) {
					return Optional.of( candidate );
				}
			}
		}
		return Optional.empty();
	}

	/**
	 * Check if the other subtree is a subset of myself.
	 * @param that the subtree to check
	 * @return true if the other subtree is a subset of myself, false otherwise
	 */
	public boolean isSubsetOf( final CERIFNode that ) {
		final String myName = getName();
		final String theirName = that.getName();		
		if ( myName.equals( theirName ) ) {
			for ( final CERIFNode myChild : getChildren( null ) ) {
				if ( ! that.lookup( myChild ).isPresent() ) {
					return false;
				}
			}
			return getValue().equals( that.getValue() );
		}
		return false;
	}

	/**
	 * Report if we miss anything the other node has.
	 * @param that the other node
	 * @return the subbranch that is missing, empty if nothing is missing
	 */
	public Optional<CERIFNode> reportWhatIMiss( CERIFNode that ) {
		final String myName = getName();
		final String theirName = that.getName();		
		if ( myName.equals( theirName ) ) {
			for ( final CERIFNode myChild : getChildren( null ) ) {
				boolean matches = false;
				CERIFNode theirFirstChild = null;
				for ( final CERIFNode theirChild : that.getChildren( myChild.getName() ) ) {
					if ( theirFirstChild == null ) {
						theirFirstChild = theirChild;
					}
					matches |= myChild.isSubsetOf( theirChild );
					if ( matches ) {
						break;
					}
				}
				if ( ! matches ) {
					return myChild.reportWhatIMiss( ( theirFirstChild != null ) ? theirFirstChild : that );
				}
			}
			return ( getValue().equals( that.getValue() ) ) ? Optional.empty() : Optional.of( this );
		}
		return Optional.of( this );
	}
	
}
