package com.jd.app.shared.helper;

import static com.jd.app.shared.constant.general.AppConstants.REQ_HEADER_IP;
import static com.jd.app.shared.constant.general.AppConstants.SESSION_ATTR_ACCESS_LOG_ID;
import static com.jd.app.shared.constant.general.AppConstants.SESSION_ATTR_AUTO_EXPIRE;
import static com.jd.app.shared.constant.general.AppConstants.SESSION_ATTR_USER_TZ;
import static com.jd.app.shared.constant.general.AppConstants.SESSION_ATTR_AUTO_LOGIN_ID;
import static com.jd.app.shared.constant.general.AppConstants.SESSION_ATTR_LOGIN_DATETIME;
import static com.jd.app.shared.constant.general.AppConstants.SESSION_ATTR_LOGIN_ID;
import static com.jd.app.shared.constant.general.AppConstants.SESSION_ATTR_USERNAME;
import static com.jd.app.shared.constant.general.AppConstants.YYYYMMDD;
import static com.jd.app.shared.constant.general.AppConstants.YYYYMMDD_HH24MMSS;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.util.WebUtils;

import com.jd.app.modules.login.bean.LoginBean;
import com.jd.app.shared.constant.enums.MediaType;
import com.jd.app.shared.constant.general.AppConstants;
import com.jd.app.shared.constant.general.CookieNames;

import is.tagomor.woothee.Classifier;

/**
 * @author Joydeep Dey
 */
public class AppUtil {

	/**
	 * Check, if a string is empty or not. A string is empty, if it is either null
	 * or its length is 0. If <code>Strict Mode</code> is enabled, string will be
	 * checked by trimming from both sides.
	 *
	 * @param str      String to be checked
	 * @param isStrict Whether to check strictly (by trimming from both side) or
	 *                 general check
	 * @return true, if the string is empty, false otherwise
	 */
	public static final boolean isEmptyString(String str, boolean isStrict) {
		return str == null ? true : (isStrict ? str.trim().isEmpty() : str.isEmpty());
	}

	/**
	 * @param date
	 * @return String format of date-time
	 */
//	public static final String dateTimeToString(Date date) {
//		return new SimpleDateFormat(AppConstants.YYYYMMDD_HH24MMSS).format(date);
//	}

	/**
	 * @param date
	 * @return String format of date-time
	 */
	public static final String localDateTimeToString(LocalDateTime datetime) {
		return datetime.format(DateTimeFormatter.ofPattern(YYYYMMDD_HH24MMSS));
	}

	/**
	 * @param date
	 * @return String format of date
	 */
	public static final String localDateToString(LocalDate date) {
		return DateTimeFormatter.ofPattern(YYYYMMDD).format(date);
	}

	/**
	 * @param date
	 * @return Date-time format of string
	 * @throws ParseException
	 */
//	public static final Date StringToDateTime(String date) {
//		Date d;
//		try {
//			d = new SimpleDateFormat(YYYYMMDD_HH24MMSS).parse(date);
//		} catch (ParseException e) {
//			d = null;
//		}
//		return d;
//	}

	/**
	 * @param date
	 * @return Date format of string
	 * @throws ParseException
	 */
//	public static final Date StringToDate(String date) {
//		Date d;
//		try {
//			d = new SimpleDateFormat(YYYYMMDD).parse(date);
//		} catch (ParseException e) {
//			d = null;
//		}
//		return d;
//	}

	/**
	 * @param date
	 * @return Date format of string
	 * @throws ParseException
	 */
	public static final LocalDate StringToLocalDate(String date) {
		LocalDate d;
		try {
			d = LocalDate.parse(date, DateTimeFormatter.ofPattern(YYYYMMDD));
		} catch (DateTimeParseException e) {
			d = null;
		}
		return d;
	}

	/**
	 * @param daysToAdd
	 * @return modified date
	 */
//	public static final Date addDaysToCurrentDate(int daysToAdd) {
//		GregorianCalendar calendar = new GregorianCalendar();
//		calendar.add(Calendar.DATE, daysToAdd);
//		return calendar.getTime();
//	}

	/**
	 * @param daysToAdd
	 * @return modified date
	 */
	public static final LocalDateTime addDaysToCurrentLocalDateTime(int daysToAdd) {
		return LocalDateTime.now().plusDays(daysToAdd);
	}

	/**
	 * @param date
	 * @param daysToAdd
	 * @return modified date
	 */
//	public static final Date addDaysToDate(Date date, int daysToAdd) {
//		GregorianCalendar calendar = new GregorianCalendar();
//		calendar.setTime(date);
//		calendar.add(Calendar.DATE, daysToAdd);
//		return calendar.getTime();
//	}

	/**
	 * Get time in milliseconds
	 *
	 * @param date1
	 * @param date2
	 * @return difference in milliseconds
	 */
//	public static final long getDateDifference(Date date1, Date date2) {
//		return date1.getTime() - date2.getTime();
//	}

	/**
	 * @param req
	 * @return IP
	 */
	public static String getClientIpAddress(HttpServletRequest request) {
		String ip = null;
		for (int i = 0; i < REQ_HEADER_IP.length; i++) {
			ip = request.getHeader(REQ_HEADER_IP[i]);
			if (ip != null && ip.length() > 0 && !ip.equalsIgnoreCase("unknown"))
				break;
		}
		if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

	/**
	 * @param bindingResult
	 * @return Error map
	 */
	public static final Map<String, String> mapFieldErrors(BindingResult bindingResult) {
		Map<String, String> errorMap = new TreeMap<>();
		if (bindingResult.getFieldErrorCount() > 0)
			bindingResult.getFieldErrors().forEach(e -> errorMap.put(e.getField(), e.getDefaultMessage()));

		if (bindingResult.getGlobalErrorCount() > 0)
			if (errorMap.isEmpty()) {
				bindingResult.getGlobalErrors().forEach(e -> errorMap.put(e.getCode(), e.getDefaultMessage()));
			}
		return errorMap;
	}

	/**
	 * @param keepHyphen
	 * @return Random UUID
	 */
	public static String generateRandomUUID(boolean keepHyphen) {
		String uuid = UUID.randomUUID().toString();
		if (!keepHyphen)
			uuid = uuid.replaceAll("\\-", "");
		return uuid;
	}

	public static void createNewSession(HttpServletRequest request, LoginBean loginBean) {
		HttpSession session = request.getSession(true);
		session.setAttribute(SESSION_ATTR_LOGIN_ID, loginBean.getLoginId());
		session.setAttribute(SESSION_ATTR_USERNAME, loginBean.getUsername());
		session.setAttribute(SESSION_ATTR_LOGIN_DATETIME, ZonedDateTime.now());
		session.setAttribute(SESSION_ATTR_ACCESS_LOG_ID, loginBean.getAccessLogId());
		session.setAttribute(SESSION_ATTR_AUTO_EXPIRE, true);
		Cookie cookie = loginBean.getCookie();
		if (cookie == null)
			return;
		String autoLoginId = cookie.getValue().split(":")[1];
		session.setAttribute(SESSION_ATTR_AUTO_LOGIN_ID, autoLoginId);
		Cookie tzCookie = WebUtils.getCookie(request, CookieNames.LANGUAGE);
		session.setAttribute(SESSION_ATTR_USER_TZ, tzCookie.getValue());
	}

	public static String encodeText(String text) {
		if (StringUtils.isBlank(text))
			return null;
		return URLEncoder.encode(text, StandardCharsets.UTF_8).replaceAll("\\+", "\\%20");
	}

	public static String decodeText(String text) {
		if (StringUtils.isBlank(text))
			return null;
		return URLDecoder.decode(text, StandardCharsets.UTF_8);
	}

	public static UserAgentBean getUserAgentDetails(String userAgent) {
		UserAgentBean userAgentBean = new UserAgentBean();
		Map<String, String> r = Classifier.parse(userAgent);

		userAgentBean.setBrowserName(r.get("name"));
		userAgentBean.setBrowserVersion(r.get("version"));
		userAgentBean.setBrowserVendor(r.get("vendor"));
		userAgentBean.setDeviceType(r.get("category"));
		userAgentBean.setOsName(r.get("os"));
		userAgentBean.setOsVersion(r.get("os_version"));

		return userAgentBean;
	}

	public static MediaType getMediaType(String filename) {
		String fileExt = filename.substring(filename.lastIndexOf("."));
		fileExt = fileExt.toLowerCase();
		for (int i = 0; i < AppConstants.IMAGE_EXTS.length; i++) {
			if (fileExt.equals(AppConstants.IMAGE_EXTS[i]))
				return MediaType.IMAGE;
		}

		for (int i = 0; i < AppConstants.AUDIO_EXTS.length; i++) {
			if (fileExt.equals(AppConstants.AUDIO_EXTS[i]))
				return MediaType.AUDIO;
		}

		for (int i = 0; i < AppConstants.VIDEO_EXTS.length; i++) {
			if (fileExt.equals(AppConstants.VIDEO_EXTS[i]))
				return MediaType.VIDEO;
		}

		for (int i = 0; i < AppConstants.TEXT_EXTS.length; i++) {
			if (fileExt.equals(AppConstants.TEXT_EXTS[i]))
				return MediaType.TEXT;
		}

		for (int i = 0; i < AppConstants.PDF_EXTS.length; i++) {
			if (fileExt.equals(AppConstants.PDF_EXTS[i]))
				return MediaType.PDF;
		}

		for (int i = 0; i < AppConstants.DOCU_EXTS.length; i++) {
			if (fileExt.equals(AppConstants.DOCU_EXTS[i]))
				return MediaType.DOCUMENT;
		}
		return null;
	}

	public static String getRelativePath(String attachmentPath) {
		attachmentPath = attachmentPath.replaceAll("\\\\", "/");
		int beginIndex = attachmentPath.indexOf(AppConstants.UPLOAD_FOLDER);
		beginIndex += AppConstants.UPLOAD_FOLDER.length();
		attachmentPath = "." + attachmentPath.substring(beginIndex);
		return attachmentPath;
	}
}
