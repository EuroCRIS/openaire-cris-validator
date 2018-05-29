package org.eurocris.openaire.cris.validator.util;

/**
 * An ad-hoc exception for the {@link CheckingIterableTest}.
 */
public abstract class MyException extends Error {

	private static final long serialVersionUID = 9075739310570035562L;

	/**
	 * New exception: no message, no cause.
	 */
	public MyException() {
		super();
	}

	/**
	 * New exception: message and cause given.
	 * @param message the message of the exception
	 * @param cause the cause of the exception
	 */
	public MyException( final String message, final Throwable cause ) {
		super( message, cause );
	}

	/**
	 * New exception: message given.
	 * @param message the message of the exception
	 */
	public MyException( final String message ) {
		super( message );
	}

	/**
	 * New exception: cause given.
	 * @param cause the cause of the exception
	 */
	public MyException( final Throwable cause ) {
		super( cause );
	}

}

/**
 * Sample 1 exception.
 */
class MyException1 extends MyException {

	private static final long serialVersionUID = -4162194931663313211L;
	
	/**
	 * Sample 1 exception: no message, no cause.
	 */
	public MyException1() {
		super();
	}

}

/**
 * Sample 2 exception.
 */
class MyException2 extends MyException {

	private static final long serialVersionUID = -4162194931663313211L;
	
	/**
	 * Sample 2 exception: no message, no cause.
	 */
	public MyException2() {
		super();
	}

}

/**
 * Sample 3 exception.
 */
class MyException3 extends MyException {

	private static final long serialVersionUID = -4162194931663313211L;
	
	/**
	 * Sample 3 exception: no message, no cause.
	 */
	public MyException3() {
		super();
	}

}