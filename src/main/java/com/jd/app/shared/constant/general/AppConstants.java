package com.jd.app.shared.constant.general;

/**
 * Various constants related to this app
 * 
 * @author Joydeep Dey
 */
public class AppConstants {

	// General Response Status
	/** Response Status: Failure/Error */
	public static final String RESP_ERROR = "-1";
	/** Response Status: Success */
	public static final String RESP_SUCCESS = "0";

	// Date Formats
	/** Date Format: 2018/08/29 */
	public static final String YYYYMMDD = "yyyy/MM/dd";
	/** Date Time Format: 2018/08/29 12:05:23 */
	public static final String YYYYMMDD_HH24MMSS = "yyyy/MM/dd HH:mm:ss";
	/** Time Format: 12:05:23 */
	public static final String HH24MMSS = "HH:mm:ss";

	// Others
	/** Line Feed */
	public static final String LF = "\n";
	/** Carriage Return Line Feed */
	public static final String CRLF = "\r\n";
	/** 0 length String */
	public static final String EMPTY_STRING = "";
	public static final String BLANK_SPACE = " ";

	// Attributes
	/** Session Attribute: Login ID */
	public static final String SESSION_ATTR_LOGIN_ID = "loginId";
	/** Session Attribute: Username */
	public static final String SESSION_ATTR_USERNAME = "username";
	/** Session Attribute: Username */
	public static final String SESSION_ATTR_LOGIN_DATETIME = "loginDateTime";
	/** Request Attribute: Login Token */
	public static final String REQ_ATTR_USER_VALID = "user-validation";

	// Cookie Related
	public static final String CK_AUTO_LOGIN = "_al";
	public static final String CK_SEPERATOR = ":";
	public static final int CK_AGE_30_DAYS = 30;
}