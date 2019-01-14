package com.jd.app.shared.interceptors;

import java.time.ZoneId;
import java.util.Enumeration;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.WebUtils;

import com.jd.app.shared.annotation.NoSessionCheck;
import com.jd.app.shared.constant.general.AppConstants;
import com.jd.app.shared.constant.general.CookieNames;
import com.jd.app.shared.helper.AppUtil;

import is.tagomor.woothee.Classifier;
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
		String userAgent = request.getHeader(AppConstants.REQ_HEADER_USER_AGENT);
		if (StringUtils.isBlank(userAgent) || Classifier.isCrawler(userAgent))
			return false;
		boolean isValid = isValidSession(request, handler);
		if (isValid)
			isValid = checkAttachmentAccess(request);

		return isValid;
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

	private static final String CERT_PATH = "/.well-known/acme-challenge/";
	private static final String[] EXCLUSION_FILES = { ".html", ".js", ".css", ".map", ".gif" };

	private static boolean isValidSession(HttpServletRequest request, Object handler) {
		String servletPath = request.getServletPath();
		if (servletPath.contains(CERT_PATH))
			return true;
		for (int i = 0; i < EXCLUSION_FILES.length; i++) {
			if (servletPath.endsWith(EXCLUSION_FILES[i]))
				return true;
		}

		Cookie tsCookie = WebUtils.getCookie(request, CookieNames.TIMEZONE);
		if (tsCookie != null && StringUtils.isNotBlank(tsCookie.getValue())) {
			TimeZone tz = TimeZone.getTimeZone(ZoneId.of(tsCookie.getValue()));
			LocaleContextHolder.setTimeZone(tz);
		}

		Cookie langCookie = WebUtils.getCookie(request, CookieNames.LANGUAGE);
		if (langCookie != null && StringUtils.isNotBlank(langCookie.getValue())) {
			String[] tagParts = langCookie.getValue().split("-");
			Locale locale;
			if (tagParts.length != 0) {
				if (tagParts.length == 1)
					locale = new Locale(tagParts[0]);
				else
					locale = new Locale(tagParts[0], tagParts[1]);
				LocaleContextHolder.setLocale(locale);
			}
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

	private static final String ATTACHMENT_PATH_REGEX = "\\/attachment\\/.+\\.[a-zA-Z0-9]{1,}";

	/**
	 * @param request
	 * @return
	 */
	private static boolean checkAttachmentAccess(HttpServletRequest request) {
		String servletPath = request.getServletPath();
		Object username = WebUtils.getSessionAttribute(request, AppConstants.SESSION_ATTR_USERNAME);

		if (username == null)
			return true;

		if (servletPath.matches(ATTACHMENT_PATH_REGEX)) {
			boolean check = (servletPath.contains("/" + username.toString() + "/"));
			if (!check)
				log.info("Illegal access detected to resource: " + request.getServletPath() + "by username: "
						+ username.toString());
			return check;
		}

		return true;
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
		String ipAddr = AppUtil.getClientIpAddress(request);
		if (ipAddr != null && !ipAddr.equals("")) {
			posted.append("&ip=" + ipAddr);
		}
		return posted.toString();
	}
}
