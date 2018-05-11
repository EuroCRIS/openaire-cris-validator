package org.eurocris.openaire.cris.validator.tree;

import org.w3c.dom.Element;

public class CERIFSimpleNode extends CERIFNode {

	private final String value;
	
	public CERIFSimpleNode( final Element el ) {
		super( el );
		this.value = el.getTextContent();
	}
	
	public String getValue() {
		return value;
	}

	public String toString( final String indent ) {
		return indent + getName() + ": " + getValue() + "\n";
	}

	public boolean isSubsetOf( final CERIFNode that ) {
		return super.isSubsetOf( that )
			&& ( that instanceof CERIFSimpleNode )
			&& getValue().equals( ( (CERIFSimpleNode) that ).getValue() );
	}

}