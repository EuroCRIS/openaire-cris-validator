package org.eurocris.openaire.cris.validator.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;

import org.w3c.dom.Node;

/**
 * A few general utility functions.
 */
public class Utils {

	/**
	 * Create an iterable that only has those elements that match a predicate.
	 * @param iterable the base iterable
	 * @param predicate the predicate to filter by
	 * @return an subset iterable
	 */
	public static <T extends Node> Iterable<T> filter( final Iterable<T> iterable, final Predicate<T> predicate ) {
		return new Iterable<T>() {

			@Override
			public Iterator<T> iterator() {
				return new Iterator<T>() {

					private final Iterator<T> parentIterator = iterable.iterator();
					private T item = null;
					private boolean itemOk = false;
					
					@Override
					public boolean hasNext() {
						while ( parentIterator.hasNext() && ! itemOk ) {
							item = parentIterator.next();
							itemOk = predicate.test( item );
						}
						return itemOk;
					}

					@Override
					public T next() {
						if ( hasNext() ) {
							itemOk = false;
							return item;
						}
						throw new NoSuchElementException();
					}
					
				};
			}
			
		};
	}
	
	/**
	 * Get the first element from an Iterable.
	 * @param iterable the iterable to draft the element from
	 * @return the first element, or an empty {@link Optional}
	 */
	public static <T> Optional<T> getFirstElement( final Iterable<T> iterable ) {
		final Iterator<T> it = iterable.iterator();
		return ( it.hasNext() ) ? Optional.of( it.next() ) : Optional.empty();
	}

}
