package com.jd.app.shared.helper;

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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.validation.BindingResult;

import com.jd.app.modules.login.bean.LoginBean;
import com.jd.app.shared.constant.general.AppConstants;

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
		return datetime.format(DateTimeFormatter.ofPattern(AppConstants.YYYYMMDD_HH24MMSS));
	}

	/**
	 * @param date
	 * @return String format of date
	 */
	public static final String localDateToString(LocalDate date) {
		return DateTimeFormatter.ofPattern(AppConstants.YYYYMMDD).format(date);
	}

	/**
	 * @param date
	 * @return Date-time format of string
	 * @throws ParseException
	 */
//	public static final Date StringToDateTime(String date) {
//		Date d;
//		try {
//			d = new SimpleDateFormat(AppConstants.YYYYMMDD_HH24MMSS).parse(date);
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
//			d = new SimpleDateFormat(AppConstants.YYYYMMDD).parse(date);
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
			d = LocalDate.parse(date, DateTimeFormatter.ofPattern(AppConstants.YYYYMMDD));
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
	public static String getClientIpAddress(HttpServletRequest req) {
		String ip = req.getHeader("X-Forwarded-For");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = req.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = req.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = req.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = req.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = req.getRemoteAddr();
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
		session.setAttribute(AppConstants.SESSION_ATTR_LOGIN_ID, loginBean.getLoginId());
		session.setAttribute(AppConstants.SESSION_ATTR_USERNAME, loginBean.getUsername());
		session.setAttribute(AppConstants.SESSION_ATTR_LOGIN_DATETIME, ZonedDateTime.now());
	}

	public static String encodeText(String text) {
		return URLEncoder.encode(text, StandardCharsets.UTF_8).replaceAll("\\+", "\\%20");
	}

	public static String decodeText(String text) {
		return URLDecoder.decode(text, StandardCharsets.UTF_8);
	}
}
