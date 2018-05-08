package org.eurocris.openaire.cris.validator.util;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import junit.framework.AssertionFailedError;

/**
 * An {@link Iterable} collection that will check certain facts either upon returning every object or after all objects have been iterated.
 * Add more checks by wrapping.
 * @author jdvorak
 *
 * @param <T> the type of the elements of the collection
 */
public abstract class CheckingIterable<T> implements Iterable<T> {

	/**
	 * Iterate through the elements and call {@link #close()} at the end.
	 * @return the number of elements iterated
	 */
	public long run() {
		long n = 0;
		final Iterator<T> it = iterator();
		while ( it.hasNext() ) {
			it.next();
			++n;
		}
		close();
		return n;
	}

	/**
	 * Extend here to place the checks to do after all the elements of the collection have been visited.
	 */
	protected abstract void close();

	/**
	 * A simple CheckingIterable that in fact doesn't check anything yet.
	 * Wrap it to do the checks.
	 * @param list
	 * @return
	 */
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

	/**
	 * Build a CheckingIterable which checks that the collection contains at least one element for which the given predicate is true.
	 * @param predicate
	 * @param error
	 * @return
	 */
	public CheckingIterable<T> checkContains( final Predicate<T> predicate, final String message ) {
		return checkContains( predicate, new AssertionFailedError( message ) );
	}
	
	/**
	 * Build a CheckingIterable which checks that the collection contains at least one element for which the given predicate is true.
	 * @param predicate
	 * @param error
	 * @return
	 */
	public CheckingIterable<T> checkContains( final Predicate<T> predicate, final Error error ) {
		final CheckingIterable<T> parentChecker = this;
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

	private static class MatchCountingIterator<T> implements Iterator<T> {
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

	/**
	 * Build a CheckingIterable which checks that the given predicate is true for all elements of the collection.
	 * @param predicate
	 * @param message
	 * @return
	 */
	public CheckingIterable<T> checkForAll( final Predicate<T> predicate, final String message ) {
		final CheckingIterable<T> parentChecker = this;
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

			@Override
			protected void close() {
				parentChecker.close();
			}

		};
	}

	/**
	 * Build a CheckingIterable which checks that the given predicate is true for all elements of the collection.
	 * @param expectedFunction
	 * @param realFunction
	 * @param message
	 * @return
	 */
	public <U> CheckingIterable<T> checkForAllEquals( final Function<T, U> expectedFunction, final Function<T, U> realFunction, final String message ) {
		return checkForAll( new Predicate<T>() {

			@Override
			public boolean test( final T obj ) {
				final U expectedValue = expectedFunction.apply( obj );
				final U realValue = realFunction.apply( obj );
				assertEquals( message, expectedValue, realValue );
				return true;
			}
			
		}, null );
	}

	/**
	 * Build a CheckingIterable which checks that the values of a function for all elements of the collection are unique.
	 * @param expectedFunction
	 * @param realFunction
	 * @param message
	 * @return
	 */
	public <U> CheckingIterable<T> checkUnique( final Function<T, U> function, final String message ) {
		final CheckingIterable<T> parentChecker = this;
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

			protected void close() {
				parentChecker.close();
			}

		};
	}

}
