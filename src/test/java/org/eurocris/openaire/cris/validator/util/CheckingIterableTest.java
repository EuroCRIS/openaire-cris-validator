package org.eurocris.openaire.cris.validator.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.junit.Test;

/**
 * Unit tests for {@link CheckingIterable}. Mostly tested on Strings.
 */
public class CheckingIterableTest {

	/**
	 * Just see if it runs on an empty collection.
	 */
	@Test
	public void testEmptyRun() {
		final List<String> list = Collections.emptyList();
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		runChecker( list, c0 );
	}

	/**
	 * Just see if it runs on a singleton list.
	 */
	@Test
	public void testSingletonRun() {
		final List<String> list = Collections.singletonList( "hello" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		runChecker( list, c0 );
	}

	/**
	 * Just see if it runs on a list of two entries.
	 */
	@Test
	public void testTwoEntriesRun() {
		final List<String> list = Arrays.asList( "hello", "world" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		runChecker( list, c0 );
	}

	/**
	 * See that we do not find an entry in an empty collection.
	 */
	@Test( expected = MyException.class)
	public void testEmptyFind() {
		final List<String> list = Collections.emptyList();
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkContains( ( s ) -> "goodbye".equals( s ), new MyException1() );
		runChecker( list, c1 );
	}

	/**
	 * See that we do find the one entry from a singleton list.
	 */
	@Test
	public void testSingletonFindYes() {
		final List<String> list = Collections.singletonList( "goodbye" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkContains( ( s ) -> "goodbye".equals( s ), new MyException1() );
		runChecker( list, c1 );
	}

	/**
	 * See that we do not find one word in a singleton list containing a different word.
	 */
	@Test( expected = MyException.class)
	public void testSingletonFindNo() {
		final List<String> list = Collections.singletonList( "hello" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkContains( ( s ) -> "goodbye".equals( s ), new MyException1() );
		runChecker( list, c1 );
	}

	/**
	 * See that we do find the first word from a two-words list.
	 */
	@Test
	public void testTwoEntriesFindFirst() {
		final List<String> list = Arrays.asList( "hello", "world" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkContains( ( s ) -> "hello".equals( s ), new MyException1() );
		runChecker( list, c1 );
	}

	/**
	 * See that we do find the second word from a two-words list.
	 */
	@Test
	public void testTwoEntriesFindSecond() {
		final List<String> list = Arrays.asList( "hello", "world" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkContains( ( s ) -> "world".equals( s ), new MyException1() );
		runChecker( list, c1 );
	}

	/**
	 * See that we do not find a word that is not contained in a two-words list.
	 */
	@Test( expected = MyException.class)
	public void testTwoEntriesFindNone() {
		final List<String> list = Arrays.asList( "hello", "world" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkContains( ( s ) -> "goodbye".equals( s ), new MyException1() );
		runChecker( list, c1 );
	}

	/**
	 * See that we do find two words out of a three-words list.
	 */
	@Test
	public void testThreeEntriesFindTwo() {
		final List<String> list = Arrays.asList( "hello", "beautiful", "world" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkContains( ( s ) -> "hello".equals( s ), new MyException1() );
		final CheckingIterable<String> c2 = c1.checkContains( ( s ) -> "world".equals( s ), new MyException2() );
		runChecker( list, c2 );
	}

	/**
	 * See that we do not find a word that is not contained in a three-words list.
	 */
	@Test( expected = MyException3.class)
	public void testThreeEntriesFindTwoButNotThird() {
		final List<String> list = Arrays.asList( "hello", "beautiful", "world" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkContains( ( s ) -> "hello".equals( s ), new MyException1() );
		final CheckingIterable<String> c2 = c1.checkContains( ( s ) -> "world".equals( s ), new MyException2() );
		final CheckingIterable<String> c3 = c2.checkContains( ( s ) -> "magical".equals( s ), new MyException3() );
		runChecker( list, c3 );
	}

	/**
	 * See that we do find all three words from a three-words list.
	 */
	@Test( expected = MyException1.class)
	public void testThreeEntriesFindTwoButNotThirdReordered() {
		final List<String> list = Arrays.asList( "hello", "beautiful", "world" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkContains( ( s ) -> "magical".equals( s ), new MyException1() );
		final CheckingIterable<String> c2 = c1.checkContains( ( s ) -> "hello".equals( s ), new MyException2() );
		final CheckingIterable<String> c3 = c2.checkContains( ( s ) -> "world".equals( s ), new MyException3() );
		runChecker( list, c3 );
	}

	/**
	 * Test that three different entries are recognized as a unique list.
	 */
	@Test
	public void testThreeEntriesUniqueOk() {
		final List<String> list = Arrays.asList( "hello", "beautiful", "world" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkUnique( Function.identity(), "Non unique word" );
		runChecker( list, c1 );		
	}
	
	/**
	 * Test that three entries, two of which are a duplicate, are recognized as a non-unique list.
	 */
	@Test( expected = AssertionError.class)
	public void testThreeEntriesUniqueFind() {
		final List<String> list = Arrays.asList( "hello", "hello", "world" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkUnique( Function.identity(), "Non unique word" );
		runChecker( list, c1 );		
	}
	
	/**
	 * Test that the uniqueness check disregards nulls, unique values listed.
	 */
	@Test
	public void testThreeEntriesUniqueOkDisregardsNulls() {
		final List<String> list = Arrays.asList( "hello", null, null, "beautiful", "world" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkUnique( Function.identity(), "Non unique word" );
		runChecker( list, c1 );		
	}
	
	/**
	 * Test that the uniqueness check disregards nulls, non-unique values listed.
	 */
	@Test( expected = AssertionError.class)
	public void testThreeEntriesUniqueFindDisregardsNulls() {
		final List<String> list = Arrays.asList( "hello", null, null, "hello", "world" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkUnique( Function.identity(), "Non unique word" );
		runChecker( list, c1 );		
	}
	
	/**
	 * Test that {@link CheckingIterable#checkForAllEquals(Function, Function, String)} works: the case of equality.
	 */
	@Test
	public void testForAllEqualsOk() {
		final List<String> list = Collections.singletonList( "beautiful" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkForAllEquals( String::toLowerCase, String::toString, "Entries should be lowercased" );
		runChecker( list, c1 );		
	}

	/**
	 * Test that {@link CheckingIterable#checkForAllEquals(Function, Function, String)} works: the case of non-equality.
	 */
	@Test( expected = AssertionError.class)
	public void testForAllEqualsFail() {
		final List<String> list = Collections.singletonList( "BeautiFul" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkForAllEquals( String::toLowerCase, String::toString, "Entries should be lowercased" );
		runChecker( list, c1 );		
	}
	
	/**
	 * Test that {@link CheckingIterable#map(Function)} works: convert to uppercase.
	 */
	@Test
	public void testMapOk() {
		final List<String> list = Arrays.asList( "hello", "beautiful", "world" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.map( String::toUpperCase );
		final CheckingIterable<String> c2 = c1.checkForAllEquals( String::toUpperCase, String::toString, "Entries should be uppercased" );
		runChecker( list, c2 );
	}
	
	/**
	 * Run the checker and see if the number of elements is preserved.
	 * @param list the list the checker was constructed from
	 * @param c1 the checker to exercise
	 */
	protected void runChecker( final List<String> list, final CheckingIterable<String> c1 ) {
		final long expected = list.size();
		final long actual = c1.run();
		if ( expected != actual ) {
			throw new IllegalStateException( "Checker run saw " + actual + " entries, but " + expected + " were expected" );
		}
	}
	
}