package com.jd.app.shared.temp;

import com.jd.app.shared.helper.AppUtil;

/**
 * @author Joydeep Dey
 */
public class UserAgentTester {

	public static void main(String[] args) {
		String userAgent = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36";

		System.out.println(AppUtil.getUserAgentDetails(userAgent).toString());
	}
}
