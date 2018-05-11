package org.eurocris.openaire.cris.validator.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.w3c.dom.Element;

public class CERIFCompoundNode extends CERIFNode {

	private final Map<String, List<CERIFNode>> data = new HashMap<>();
	
	public CERIFCompoundNode( final Element el ) {
		super( el );
	}

	public void addChild( final CERIFNode node ) {
		final String name = node.getName();
		final List<CERIFNode> newList = new ArrayList<>(1);
		final List<CERIFNode> oldList = data.putIfAbsent( name, newList );
		final List<CERIFNode> list = ( oldList != null ) ? oldList : newList;
		list.add( node );
	}
	
	public Iterable<CERIFNode> getChildren() {
		return new Iterable<CERIFNode>() {

			private final Iterator<List<CERIFNode>> it1 = data.values().iterator();
			
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

	public String toString( final String indent ) {
		final StringBuilder sb = new StringBuilder( indent + getName() + ":\n" );
		final String newIndent = "  " + indent;
		for ( final CERIFNode node : getChildren() ) {
			sb.append( node.toString( newIndent ) );
		}
		return sb.toString();
	}
	
	public Optional<CERIFNode> lookup( final CERIFNode what ) {
		final String whatName = what.getName();
		final List<CERIFNode> candidates = data.get( whatName );
		if ( candidates != null ) {
			for ( final CERIFNode candidate : candidates ) {
				if ( what.isSubsetOf( candidate ) ) {
					return Optional.ofNullable( candidate );
				}
			}
		}
		return Optional.empty();
	}

	public boolean isSubsetOf( final CERIFNode that ) {
		if ( super.isSubsetOf( that ) && ( that instanceof CERIFCompoundNode ) ) {
			for ( final CERIFNode myChild : getChildren() ) {
				if ( ! ( (CERIFCompoundNode) that ).lookup( myChild ).isPresent() ) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
}
