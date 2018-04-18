package org.eurocris.openaire.cris.validator.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.junit.Test;

public class CheckingIterableTest {

	@Test
	public void testEmptyRun() {
		final List<String> list = Collections.emptyList();
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		runChecker( list, c0 );
	}

	@Test
	public void testSingletonRun() {
		final List<String> list = Collections.singletonList( "hello" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		runChecker( list, c0 );
	}

	@Test
	public void testTwoEntriesRun() {
		final List<String> list = Arrays.asList( "hello", "world" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		runChecker( list, c0 );
	}

	@Test( expected = MyException.class)
	public void testEmptyFind() {
		final List<String> list = Collections.emptyList();
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkContains( ( s ) -> "goodbye".equals( s ), new MyException1() );
		runChecker( list, c1 );
	}

	@Test
	public void testSingletonFindYes() {
		final List<String> list = Collections.singletonList( "goodbye" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkContains( ( s ) -> "goodbye".equals( s ), new MyException1() );
		runChecker( list, c1 );
	}

	@Test( expected = MyException.class)
	public void testSingletonFindNo() {
		final List<String> list = Collections.singletonList( "hello" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkContains( ( s ) -> "goodbye".equals( s ), new MyException1() );
		runChecker( list, c1 );
	}

	@Test
	public void testTwoEntriesFindFirst() {
		final List<String> list = Arrays.asList( "hello", "world" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkContains( ( s ) -> "hello".equals( s ), new MyException1() );
		runChecker( list, c1 );
	}

	@Test
	public void testTwoEntriesFindSecond() {
		final List<String> list = Arrays.asList( "hello", "world" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkContains( ( s ) -> "world".equals( s ), new MyException1() );
		runChecker( list, c1 );
	}

	@Test( expected = MyException.class)
	public void testTwoEntriesFindNone() {
		final List<String> list = Arrays.asList( "hello", "world" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkContains( ( s ) -> "goodbye".equals( s ), new MyException1() );
		runChecker( list, c1 );
	}

	@Test
	public void testThreeEntriesFindTwo() {
		final List<String> list = Arrays.asList( "hello", "beautiful", "world" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkContains( ( s ) -> "hello".equals( s ), new MyException1() );
		final CheckingIterable<String> c2 = c1.checkContains( ( s ) -> "world".equals( s ), new MyException2() );
		runChecker( list, c2 );
	}

	@Test( expected = MyException3.class)
	public void testThreeEntriesFindTwoButNotThird() {
		final List<String> list = Arrays.asList( "hello", "beautiful", "world" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkContains( ( s ) -> "hello".equals( s ), new MyException1() );
		final CheckingIterable<String> c2 = c1.checkContains( ( s ) -> "world".equals( s ), new MyException2() );
		final CheckingIterable<String> c3 = c2.checkContains( ( s ) -> "magical".equals( s ), new MyException3() );
		runChecker( list, c3 );
	}

	@Test( expected = MyException1.class)
	public void testThreeEntriesFindTwoButNotThirdReordered() {
		final List<String> list = Arrays.asList( "hello", "beautiful", "world" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkContains( ( s ) -> "magical".equals( s ), new MyException1() );
		final CheckingIterable<String> c2 = c1.checkContains( ( s ) -> "hello".equals( s ), new MyException2() );
		final CheckingIterable<String> c3 = c2.checkContains( ( s ) -> "world".equals( s ), new MyException3() );
		runChecker( list, c3 );
	}

	@Test
	public void testThreeEntriesUniqueOk() {
		final List<String> list = Arrays.asList( "hello", "beautiful", "world" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkUnique( Function.identity(), "Non unique word" );
		runChecker( list, c1 );		
	}
	
	@Test( expected = AssertionError.class)
	public void testThreeEntriesUniqueFind() {
		final List<String> list = Arrays.asList( "hello", "hello", "world" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkUnique( Function.identity(), "Non unique word" );
		runChecker( list, c1 );		
	}
	
	@Test
	public void testThreeEntriesUniqueOkDisregardsNulls() {
		final List<String> list = Arrays.asList( "hello", null, null, "beautiful", "world" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkUnique( Function.identity(), "Non unique word" );
		runChecker( list, c1 );		
	}
	
	@Test( expected = AssertionError.class)
	public void testThreeEntriesUniqueFindDisregardsNulls() {
		final List<String> list = Arrays.asList( "hello", null, null, "hello", "world" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkUnique( Function.identity(), "Non unique word" );
		runChecker( list, c1 );		
	}
	
	@Test
	public void testForAllEqualsOk() {
		final List<String> list = Collections.singletonList( "beautiful" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkForAllEquals( String::toLowerCase, String::toString, "Entries should be lowercased" );
		runChecker( list, c1 );		
	}

	@Test( expected = AssertionError.class)
	public void testForAllEqualsFail() {
		final List<String> list = Collections.singletonList( "BeautiFul" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkForAllEquals( String::toLowerCase, String::toString, "Entries should be lowercased" );
		runChecker( list, c1 );		
	}
	
	protected void runChecker( final List<String> list, final CheckingIterable<String> c1 ) {
		final long expected = list.size();
		final long actual = c1.run();
		if ( expected != actual ) {
			throw new IllegalStateException( "Checker run saw " + actual + " entries, but " + expected + " were expected" );
		}
	}
	
}