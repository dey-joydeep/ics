package com.jd.app.shared.error.exceptions;

/**
 * @author Joydeep Dey
 */
public class ResourceNotFoundException extends Exception {

	private static final long serialVersionUID = 8696469967282701620L;

	/**
	 * 
	 */
	public ResourceNotFoundException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ResourceNotFoundException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public ResourceNotFoundException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public ResourceNotFoundException(final Throwable cause) {
		super(cause);
	}
}
