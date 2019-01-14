package com.jd.app.shared.temp;

import com.jd.app.shared.crypto.CryptoUtil;
import com.jd.app.shared.error.exceptions.CryptoException;

public class HashMaker {

	public static void main(String[] args) throws CryptoException {
		String password = "barun";
		String hashedPassword = CryptoUtil.createPwHash(password);
		System.out.println(hashedPassword);
	}
}
