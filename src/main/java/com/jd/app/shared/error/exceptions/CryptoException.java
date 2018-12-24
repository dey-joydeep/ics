package com.jd.app.shared.error.exceptions;

/**
 * @author Joydeep Dey
 */
public class CryptoException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * @param message
	 */
	public CryptoException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param source
	 */
	public CryptoException(String message, Throwable source) {
		super(message, source);
	}
}
