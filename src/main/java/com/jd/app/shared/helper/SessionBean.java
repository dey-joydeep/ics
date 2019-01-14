package com.jd.app.shared.helper;

import static com.jd.app.shared.constant.general.AppConstants.SESSION_ATTR_ACCESS_LOG_ID;
import static com.jd.app.shared.constant.general.AppConstants.SESSION_ATTR_AUTO_LOGIN_ID;
import static com.jd.app.shared.constant.general.AppConstants.SESSION_ATTR_LOGIN_DATETIME;
import static com.jd.app.shared.constant.general.AppConstants.SESSION_ATTR_LOGIN_ID;
import static com.jd.app.shared.constant.general.AppConstants.SESSION_ATTR_USERNAME;
import static com.jd.app.shared.constant.general.AppConstants.SESSION_ATTR_AUTO_EXPIRE;

import java.time.ZonedDateTime;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author Joydeep Dey
 */
public class SessionBean {

	private HttpSession session;

	/**
	 * 
	 */
	public SessionBean(HttpServletRequest request) {
		this.session = request.getSession(false);
	}

	/**
	 * 
	 */
	public SessionBean(HttpSession session) {
		this.session = session;
	}

	public long getLoginId() {
		Object ob = session.getAttribute(SESSION_ATTR_LOGIN_ID);
		return ob == null ? null : Long.parseLong(ob.toString());
	}

	public String getUserName() {
		Object ob = session.getAttribute(SESSION_ATTR_USERNAME);
		return ob == null ? null : ob.toString();
	}

	public ZonedDateTime getLoginDatetime() {
		Object ob = session.getAttribute(SESSION_ATTR_LOGIN_DATETIME);
		return ob == null ? null : (ZonedDateTime) ob;
	}

	public long getAccessLogId() {
		Object ob = session.getAttribute(SESSION_ATTR_ACCESS_LOG_ID);
		return ob == null ? null : Long.parseLong(ob.toString());
	}

	public String getAutoLoginId() {
		Object ob = session.getAttribute(SESSION_ATTR_AUTO_LOGIN_ID);
		return ob == null ? null : ob.toString();
	}

	public boolean isAutoExpired() {
		Object ob = session.getAttribute(SESSION_ATTR_AUTO_EXPIRE);
		return ob == null ? null : Boolean.parseBoolean(ob.toString());
	}
}
