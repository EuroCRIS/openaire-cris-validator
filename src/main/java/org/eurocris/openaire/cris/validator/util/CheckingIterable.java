package org.eurocris.openaire.cris.validator.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import junit.framework.AssertionFailedError;

public class CheckingIterable<T> implements Iterable<T> {

	public static <T> CheckingIterable<T> over( final Iterable<T> list ) {
		return new CheckingIterable<T>() {

			@Override
			public Iterator<T> iterator() {
				return list.iterator();
			}

			@Override
			protected void close() {
				// no-op
			}
			
		};
	}

	public CheckingIterable<T> checkContains( final Predicate<T> predicate, final Error error ) {
		final CheckingIterable<T> parentChecker = (CheckingIterable<T>) this;
		return new CheckingIterable<T>() {

			MatchCountingIterator<T> mci;

			@Override
			public Iterator<T> iterator() {
				return mci = new MatchCountingIterator<T>( parentChecker.iterator(), predicate );
			}

			@Override
			protected void close() {
				parentChecker.close();
				if ( mci.getCount() == 0L ) {
					throw error;
				}
			}

		};
	}

	public CheckingIterable<T> checkForAll( final Predicate<T> predicate, final String message ) {
		final CheckingIterable<T> parentChecker = (CheckingIterable<T>) this;
		return new CheckingIterable<T>() {

			@Override
			public Iterator<T> iterator() {
				final Iterator<T> parentIterator = parentChecker.iterator();
				return new Iterator<T>() {

					@Override
					public boolean hasNext() {
						return parentIterator.hasNext();
					}

					@Override
					public T next() {
						final T obj = parentIterator.next();
						final boolean match = predicate.test( obj );
						if ( ! match ) {
							throw new AssertionFailedError( message + "; object: " + obj );
						}
						return obj;
					}

				};
			}

		};
	}

	public <U> CheckingIterable<T> checkUnique( final Function<T, U> function, final String message ) {
		final CheckingIterable<T> parentChecker = (CheckingIterable<T>) this;
		final Set<U> seenValues = new HashSet<>();
		return new CheckingIterable<T>() {

			@Override
			public Iterator<T> iterator() {
				final Iterator<T> parentIterator = parentChecker.iterator();
				return new Iterator<T>() {

					@Override
					public boolean hasNext() {
						return parentIterator.hasNext();
					}

					@Override
					public T next() {
						final T obj = parentIterator.next();
						final U val = function.apply( obj );
						if ( val != null && !seenValues.add( val ) ) {
							throw new AssertionFailedError( message + "; value: " + val );
						}
						return obj;
					}

				};
			}

		};
	}

	protected void close() {
		// no-op
	}

	public Iterator<T> iterator() {
		return null;
	}

	public long run() {
		long n = 0;
		for ( @SuppressWarnings( "unused")
		final T x : this ) {
			++n;
		}
		close();
		return n;
	}

}

class MatchCountingIterator<T> implements Iterator<T> {
	private final Predicate<T> predicate;
	private final Iterator<T> parent;
	private long count = 0;

	MatchCountingIterator( final Iterator<T> parent, final Predicate<T> predicate ) {
		this.predicate = predicate;
		this.parent = parent;
	}

	@Override
	public boolean hasNext() {
		return parent.hasNext();
	}

	@Override
	public T next() {
		final T obj = parent.next();
		if ( predicate.test( obj ) ) {
			++count;
		}
		return obj;
	}

	public long getCount() {
		return count;
	}

}
