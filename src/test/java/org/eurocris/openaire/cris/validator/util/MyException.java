package org.eurocris.openaire.cris.validator.util;

public abstract class MyException extends Error {

	private static final long serialVersionUID = 9075739310570035562L;

	public MyException() {
		super();
	}

	public MyException( final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace ) {
		super( message, cause, enableSuppression, writableStackTrace );
	}

	public MyException( final String message, final Throwable cause ) {
		super( message, cause );
	}

	public MyException( final String message ) {
		super( message );
	}

	public MyException( final Throwable cause ) {
		super( cause );
	}

}

class MyException1 extends MyException {

	private static final long serialVersionUID = -4162194931663313211L;
	
	public MyException1() {
		super();
	}

	public MyException1( final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace ) {
		super( message, cause, enableSuppression, writableStackTrace );
	}

	public MyException1( final String message, final Throwable cause ) {
		super( message, cause );
	}

	public MyException1( final String message ) {
		super( message );
	}

	public MyException1( final Throwable cause ) {
		super( cause );
	}
	
}

class MyException2 extends MyException {

	private static final long serialVersionUID = -4162194931663313211L;
	
	public MyException2() {
		super();
	}

	public MyException2( final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace ) {
		super( message, cause, enableSuppression, writableStackTrace );
	}

	public MyException2( final String message, final Throwable cause ) {
		super( message, cause );
	}

	public MyException2( final String message ) {
		super( message );
	}

	public MyException2( final Throwable cause ) {
		super( cause );
	}
	
}

class MyException3 extends MyException {

	private static final long serialVersionUID = -4162194931663313211L;
	
	public MyException3() {
		super();
	}

	public MyException3( final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace ) {
		super( message, cause, enableSuppression, writableStackTrace );
	}

	public MyException3( final String message, final Throwable cause ) {
		super( message, cause );
	}

	public MyException3( final String message ) {
		super( message );
	}

	public MyException3( final Throwable cause ) {
		super( cause );
	}
	
}