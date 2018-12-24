package com.jd.app.shared.crypto;

import com.jd.app.shared.error.exceptions.CryptoException;

/**
 * @author Joydeep Dey
 */
public class CryptoUtil {

	/**
	 * @param password
	 * @return BCrypt 15 round hashed string
	 * @throws CryptoException
	 */
	public static final String createPwHash(String password) throws CryptoException {
		String generatedSecuredPasswordHash = null;
		try {
			generatedSecuredPasswordHash = BCrypt.hashpw(password, BCrypt.gensalt(15));
		} catch (Exception e) {
			throw new CryptoException("Error occured during password hash creation.", e);
		}
		return generatedSecuredPasswordHash;

	}

	/**
	 * @param userPassword
	 * @param dbPassword
	 * @return verification result
	 * @throws CryptoException
	 */
	public static final boolean verifyPassword(String userPassword, String dbPassword) throws CryptoException {
		boolean matched = false;

		try {
			matched = BCrypt.checkpw(userPassword, dbPassword);
		} catch (Exception e) {
			throw new CryptoException("Error occured during password verification.", e);
		}
		return matched;
	}

	public static final String createHash(String value, int saltLen) throws CryptoException {
		String generatedSecuredPasswordHash = null;
		try {
			generatedSecuredPasswordHash = BCrypt.hashpw(value, BCrypt.gensalt(saltLen));
		} catch (Exception e) {
			throw new CryptoException("Error occured during hash creation.", e);
		}
		return generatedSecuredPasswordHash;
	}

	public static final boolean verifyHash(String toVerify, String verifyWith) throws CryptoException {
		boolean matched = false;

		try {
			matched = BCrypt.checkpw(toVerify, verifyWith);
		} catch (Exception e) {
			throw new CryptoException("Error occured during verification.", e);
		}
		return matched;
	}
}
