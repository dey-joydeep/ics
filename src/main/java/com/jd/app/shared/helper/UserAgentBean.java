package com.jd.app.shared.helper;

import lombok.Data;

/**
 * @author Joydeep Dey
 */
@Data
public class UserAgentBean {

	private String browserName;
	private String browserVersion;
	private String browserVendor;
	private String deviceType;
	private String osName;
	private String osVersion;
}
