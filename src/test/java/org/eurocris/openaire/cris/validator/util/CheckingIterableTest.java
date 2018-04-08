package org.eurocris.openaire.cris.validator.util;

import static org.junit.Assert.*;

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
		assertEquals( list.size(), c0.run() );
	}

	@Test
	public void testSingletonRun() {
		final List<String> list = Collections.singletonList( "hello" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		assertEquals( list.size(), c0.run() );
	}

	@Test
	public void testTwoEntriesRun() {
		final List<String> list = Arrays.asList( "hello", "world" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		assertEquals( list.size(), c0.run() );
	}

	@Test( expected = MyException.class)
	public void testEmptyFind() {
		final List<String> list = Collections.emptyList();
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkContains( ( s ) -> "goodbye".equals( s ), new MyException1() );
		assertEquals( list.size(), c1.run() );
	}

	@Test
	public void testSingletonFindYes() {
		final List<String> list = Collections.singletonList( "goodbye" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkContains( ( s ) -> "goodbye".equals( s ), new MyException1() );
		assertEquals( list.size(), c1.run() );
	}

	@Test( expected = MyException.class)
	public void testSingletonFindNo() {
		final List<String> list = Collections.singletonList( "hello" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkContains( ( s ) -> "goodbye".equals( s ), new MyException1() );
		assertEquals( list.size(), c1.run() );
	}

	@Test
	public void testTwoEntriesFindFirst() {
		final List<String> list = Arrays.asList( "hello", "world" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkContains( ( s ) -> "hello".equals( s ), new MyException1() );
		assertEquals( list.size(), c1.run() );
	}

	@Test
	public void testTwoEntriesFindSecond() {
		final List<String> list = Arrays.asList( "hello", "world" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkContains( ( s ) -> "world".equals( s ), new MyException1() );
		assertEquals( list.size(), c1.run() );
	}

	@Test( expected = MyException.class)
	public void testTwoEntriesFindNone() {
		final List<String> list = Arrays.asList( "hello", "world" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkContains( ( s ) -> "goodbye".equals( s ), new MyException1() );
		assertEquals( list.size(), c1.run() );
	}

	@Test
	public void testThreeEntriesFindTwo() {
		final List<String> list = Arrays.asList( "hello", "beautiful", "world" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkContains( ( s ) -> "hello".equals( s ), new MyException1() );
		final CheckingIterable<String> c2 = c1.checkContains( ( s ) -> "world".equals( s ), new MyException2() );
		assertEquals( list.size(), c2.run() );
	}

	@Test( expected = MyException3.class)
	public void testThreeEntriesFindTwoButNotThird() {
		final List<String> list = Arrays.asList( "hello", "beautiful", "world" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkContains( ( s ) -> "hello".equals( s ), new MyException1() );
		final CheckingIterable<String> c2 = c1.checkContains( ( s ) -> "world".equals( s ), new MyException2() );
		final CheckingIterable<String> c3 = c2.checkContains( ( s ) -> "magical".equals( s ), new MyException3() );
		assertEquals( list.size(), c3.run() );
	}

	@Test( expected = MyException1.class)
	public void testThreeEntriesFindTwoButNotThirdReordered() {
		final List<String> list = Arrays.asList( "hello", "beautiful", "world" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkContains( ( s ) -> "magical".equals( s ), new MyException1() );
		final CheckingIterable<String> c2 = c1.checkContains( ( s ) -> "hello".equals( s ), new MyException2() );
		final CheckingIterable<String> c3 = c2.checkContains( ( s ) -> "world".equals( s ), new MyException3() );
		assertEquals( list.size(), c3.run() );
	}

	@Test
	public void testThreeEntriesUniqueOk() {
		final List<String> list = Arrays.asList( "hello", "beautiful", "world" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkUnique( Function.identity(), "Non unique word" );
		assertEquals( list.size(), c1.run() );		
	}
	
	@Test( expected = AssertionError.class)
	public void testThreeEntriesUniqueFind() {
		final List<String> list = Arrays.asList( "hello", "hello", "world" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkUnique( Function.identity(), "Non unique word" );
		assertEquals( list.size(), c1.run() );		
	}
	
	@Test
	public void testThreeEntriesUniqueOkDisregardsNulls() {
		final List<String> list = Arrays.asList( "hello", null, null, "beautiful", "world" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkUnique( Function.identity(), "Non unique word" );
		assertEquals( list.size(), c1.run() );		
	}
	
	@Test( expected = AssertionError.class)
	public void testThreeEntriesUniqueFindDisregardsNulls() {
		final List<String> list = Arrays.asList( "hello", null, null, "hello", "world" );
		final CheckingIterable<String> c0 = CheckingIterable.over( list );
		final CheckingIterable<String> c1 = c0.checkUnique( Function.identity(), "Non unique word" );
		assertEquals( list.size(), c1.run() );		
	}
	
}