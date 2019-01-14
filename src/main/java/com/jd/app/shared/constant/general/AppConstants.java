package com.jd.app.shared.constant.general;

import java.io.File;

/**
 * Various constants related to this app
 * 
 * @author Joydeep Dey
 */
public class AppConstants {

	// File upload root path
	public static final String UPLOAD_FOLDER = "upload";
	public static final String ATTACHMENT_FOLDER = "attachment/";
	public static final String UPLOAD_PATH = "C:/application/" + UPLOAD_FOLDER + "/";

	// Create root path directories, if doen't exist
	static {
		File f = new File(UPLOAD_PATH);
		if (!f.exists())
			f.mkdirs();
	}

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
	/** 0 length trimmed String */
	public static final String EMPTY_STRING = "";
	public static final String BLANK_SPACE = " ";

	// Attributes
	/** Session Attribute: Login ID */
	public static final String SESSION_ATTR_LOGIN_ID = "loginId";
	/** Session Attribute: Username */
	public static final String SESSION_ATTR_USERNAME = "username";
	/** Session Attribute: login date-time */
	public static final String SESSION_ATTR_LOGIN_DATETIME = "loginDateTime";
	/** Session Attribute: Access log ID */
	public static final String SESSION_ATTR_ACCESS_LOG_ID = "accessLogId";
	/** Session Attribute: Auto login ID */
	public static final String SESSION_ATTR_AUTO_LOGIN_ID = "autoLoginId";
	/** Session Attribute: Auto expire, default: true */
	public static final String SESSION_ATTR_AUTO_EXPIRE = "autoExpire";
	/** WebSocket Session Attribute: HTTP Session ID */
	public static final String WS_SESSION_ATTR_ID = "HTTP.SESSION.ID";
	/** Session Attribute: User's time-zone */
	public static final String SESSION_ATTR_USER_TZ = "user-tz";
	/** Request Attribute: Login Token */
	public static final String REQ_ATTR_USER_VALID = "user-validation";

	// Headers
	/** Request Header: User Agent */
	public static final String REQ_HEADER_USER_AGENT = "User-Agent";
	public static final String[] REQ_HEADER_IP = { "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP",
			"HTTP_X_FORWARDED_FOR", "HTTP_X_FORWARDED", "HTTP_X_CLUSTER_CLIENT_IP", "HTTP_CLIENT_IP",
			"HTTP_FORWARDED_FOR", "HTTP_FORWARDED", "HTTP_VIA", "REMOTE_ADDR" };
	// Cookie Related
	public static final String CK_AUTO_LOGIN = "_al";
	public static final String CK_SEPERATOR = ":";
	public static final int CK_AGE_30_DAYS = 30;

	// Others
	public static final int MAX_ACCESS_LOG = 10;

	public static final String IMAGE_EXT_GIF = ".gif";
	public static final String IMAGE_EXT_JPG = ".jpg";
	public static final String[] IMAGE_EXTS = { ".gif", ".png", ".jpg", ".jpeg" };
	public static final String[] AUDIO_EXTS = { ".ogg", ".mp3", ".aac", ".mid", ".wav" };
	public static final String[] VIDEO_EXTS = { ".ogg", ".mpg", ".mpeg", ".mov", ".mp4", ".3gp", ".flv", ".avi" };
	public static final String[] TEXT_EXTS = { ".txt", ".c", ".java" };
	public static final String[] PDF_EXTS = { ".pdf" };
	public static final String[] DOCU_EXTS = { ".docx", ".doc", ".xls", "xlsx", ".txt", ".c", ".java", ".pdf", ".ppt",
			"pptx", ".zip" };
}