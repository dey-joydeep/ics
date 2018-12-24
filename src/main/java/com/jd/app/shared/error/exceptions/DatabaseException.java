package com.jd.app.shared.error.exceptions;

/**
 * @author Joydeep Dey
 */
public class DatabaseException extends Exception {

	private static final long serialVersionUID = -1327640756790659188L;

	/**
	 * 
	 */
	public DatabaseException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DatabaseException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public DatabaseException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public DatabaseException(final Throwable cause) {
		super(cause);
	}
}
