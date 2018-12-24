package com.jd.app.shared.interceptors;

import java.time.ZoneId;
import java.util.Enumeration;
import java.util.TimeZone;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.WebUtils;

import com.jd.app.shared.annotation.NoSessionCheck;
import com.jd.app.shared.constant.general.AppConstants;
import com.jd.app.shared.helper.AppUtil;

import lombok.extern.log4j.Log4j2;

/**
 * @author Joydeep Dey
 */
@Log4j2
public class RequestHandleInterceptor extends HandlerInterceptorAdapter {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		log.info("[preHandle][" + request + "]" + "[" + request.getMethod() + "]" + request.getRequestURI()
				+ getParameters(request));
		log.info("Header Details: " + getHeaders(request));
		Cookie cookie = WebUtils.getCookie(request, "ts");
		if (cookie != null)
			setThreadTimezone(cookie.getValue());
		return isValidSession(request, handler);
	}

	private static void setThreadTimezone(String tzName) {
		try {
			TimeZone tz = TimeZone.getTimeZone(ZoneId.of(tzName));
			LocaleContextHolder.setTimeZone(tz);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {

		// Set standard HTTP/1.1 no-cache headers.
		response.setHeader("Cache-Control", "no-store, no-cache, no-transform, must-revalidate");

		// Set IE extended HTTP/1.1 no-cache headers (use addHeader).
		response.addHeader("Cache-Control", "post-check=0, pre-check=0");

		// Proxy
		response.setDateHeader("Expires", 0);
	}

	private static final String[] EXCLUSION_FILES = { ".html", ".js", ".css", ".map", ".gif" };

	private static boolean isValidSession(HttpServletRequest request, Object handler) {
		String servletPath = request.getServletPath();
		for (int i = 0; i < EXCLUSION_FILES.length; i++) {
			if (servletPath.endsWith(EXCLUSION_FILES[i]))
				return true;
		}
		if (handler instanceof HandlerMethod) {
			HandlerMethod method = (HandlerMethod) handler;
			if (method.getMethod().isAnnotationPresent(NoSessionCheck.class)
					|| method.getMethod().getDeclaringClass().isAnnotationPresent(NoSessionCheck.class)) {
				return true;
			}
		}

		HttpSession session = request.getSession(false);
		boolean isValid = (session != null && (session.getAttribute(AppConstants.SESSION_ATTR_USERNAME) != null));

		request.setAttribute(AppConstants.REQ_ATTR_USER_VALID, isValid);
		if (servletPath.equals("/"))
			isValid = true;
		if (!isValid)
			log.warn("Aceess to path : " + servletPath + " detected, without login.");

		return isValid;
	}

	private static String getParameters(HttpServletRequest request) {
		StringBuilder posted = new StringBuilder();
		Enumeration<?> e = request.getParameterNames();
		if (e != null) {
			posted.append("?");

			while (e.hasMoreElements()) {
				if (posted.length() > 1) {
					posted.append("&");
				}
				String curr = (String) e.nextElement();
				posted.append(curr + "=");
				posted.append(request.getParameter(curr));
			}
		}
		String ip = request.getHeader("X-FORWARDED-FOR");
		String ipAddr = (ip == null) ? AppUtil.getClientIpAddress(request) : ip;
		if (ipAddr != null && !ipAddr.equals("")) {
			posted.append("&ip=" + ipAddr);
		}
		return posted.toString();
	}

	private static String getHeaders(HttpServletRequest request) {
		StringBuilder headers = new StringBuilder();
		Enumeration<?> e = request.getHeaderNames();
		if (e != null) {
			while (e.hasMoreElements()) {
				String header = (String) e.nextElement();
				headers.append(header);
				headers.append("=");
				headers.append(request.getHeader(header));
				headers.append(", ");
			}
		}
		return headers.toString();
	}
}
