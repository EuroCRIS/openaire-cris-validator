package org.eurocris.openaire.cris.validator.tree;

import java.util.ArrayList;
import java.util.List;

import org.eurocris.openaire.cris.validator.util.XmlUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public abstract class CERIFNode {
	
	private final String type;
	
	private final String name;
	
	public CERIFNode( final Element el ) {
		final StringBuilder sb = new StringBuilder( el.getLocalName() );
		this.type = sb.toString().intern();
		final NamedNodeMap attributes = el.getAttributes();
		if ( attributes != null ) {
			final int n = attributes.getLength();
			for ( int i = 0; i < n; ++i ) {
				final Attr attr = (Attr) attributes.item( i );
				final String nsURI = attr.getNamespaceURI();
				if ( ! "http://www.w3.org/2000/xmlns/".equals( nsURI ) ) {
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
	}

	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}
	
	public static CERIFNode buildTree( final Element node ) {
		final CERIFCompoundNode result = new CERIFCompoundNode( (Element) node );
		boolean childElementsExist = false;
		for ( final Node node2 : XmlUtils.nodeListToIterableOfNodes( node.getChildNodes() ) ) {
			if ( node2 instanceof Element ) {
				final CERIFNode x2 = CERIFNode.buildTree( (Element) node2 );
				result.addChild( x2 );
				childElementsExist = true;
			}
		}
		if ( childElementsExist ) {
			return result;
		} else {
			return new CERIFSimpleNode( node );				
		}
	}
	
	public abstract String toString( final String indent );

	@Override
	public String toString() {
		return toString( "- " );
	}
	
	public List<String> checkIsSubsetOf( final CERIFNode that ) {
		final String myName = getName();
		final String theirName = that.getName();
		final List<String> result = new ArrayList<>(0);
		if ( myName.equals( theirName ) ) {
			result.add( "Names do not match: " + myName + " vs " + theirName );
		}
		return result;
	}

	public boolean isSubsetOf( final CERIFNode that ) {
		final String myName = getName();
		final String theirName = that.getName();		
		return ( myName.equals( theirName ) );
	}

}
