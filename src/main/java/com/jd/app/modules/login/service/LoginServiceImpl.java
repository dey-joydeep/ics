package com.jd.app.modules.login.service;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jd.app.db.dao.def.AccessLogDao;
import com.jd.app.db.dao.def.LoginDao;
import com.jd.app.db.dao.def.LoginSessionDao;
import com.jd.app.db.entity.AccessLog;
import com.jd.app.db.entity.Login;
import com.jd.app.db.entity.LoginSession;
import com.jd.app.modules.login.bean.LoginBean;
import com.jd.app.shared.constant.general.AppConstants;
import com.jd.app.shared.crypto.CryptoUtil;
import com.jd.app.shared.helper.FormValidator;
import com.jd.app.shared.helper.SessionBean;

import lombok.extern.log4j.Log4j2;

/**
 * @author Joydeep Dey
 */
@Log4j2
@Service
public class LoginServiceImpl implements LoginService {

	@Autowired
	private LoginDao loginDao;
	@Autowired
	private LoginSessionDao loginSessionDao;
	@Autowired
	private AccessLogDao accessLogDao;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void authenticate(LoginBean loginBean) {

		ZonedDateTime loginSessionExpiryAt = null;
		String username = loginBean.getUsername();
		String loginToken = loginBean.getLoginToken();
		String userIpAddress = loginBean.getIpAddress();
		Login login = new Login();
		try {
			if (loginToken != null) {
				// verify login session by cookies
				String[] tokens = loginToken.split(AppConstants.CK_SEPERATOR);
				long loginId = Long.valueOf(tokens[0]);
				LoginSession entity = new LoginSession();
				login.setId(loginId);
				entity.setLogin(login);
				entity.setSessionId(tokens[1]);
				entity.setIpAddress(loginBean.getIpAddress());
				entity = loginSessionDao.getLoginSessionDetails(entity);

				// If no info is found(wrong token/token expired/auto login disabled),
				// redirect to login page to ask for login by login id and password
				if (entity == null)
					return;

				login = entity.getLogin();
				loginBean.setUsername(login.getUser().getUsername());
				loginSessionExpiryAt = entity.getSessionExpiryAt();
				// Delete the old cookie for this device and generate a new one
				loginSessionDao.deleteLoginSession(tokens[1]);

				// If the session cookie expires, return to login page
				if (loginSessionExpiryAt.isBefore(ZonedDateTime.now()))
					return;

				loginBean.setRememberMe(true);
			} else {
				Map<String, String> errors = FormValidator.validateLoginForm(loginBean);
				if (!errors.isEmpty()) {
					loginBean.setErrors(errors);
					return;
				}
				login = loginDao.getLoginDetails(loginBean.getUsername());
				if (login == null) {
					loginBean.setMessage("Username/Password does not match");
					return;
				}
				String allowedIp = login.getAllowedIp();
				if (login.isIpRestricted() && allowedIp != null && !allowedIp.equals(userIpAddress)) {
					loginBean.setMessage("User is not allowed to access the service from IP address: " + userIpAddress);
					return;
				}

				String password = loginBean.getPassword();
				if (!CryptoUtil.verifyPassword(password, login.getPassword())) {
					loginBean.setMessage("Username/Password does not match");
					return;
				}
				if (login.isIpRestricted() && allowedIp == null) {
					login.setAllowedIp(userIpAddress);
					loginDao.updateLoginDetails(login);
				}
			}

			AccessLog accessLog = new AccessLog();
			accessLog.setLogin(login);
			accessLog.setAccessedAt(Instant.now());
			accessLog.setUserAgent(loginBean.getUserAgent());
			accessLog.setIpAddress(loginBean.getIpAddress());

			loginBean.setLoginId(login.getId());

			if (loginBean.isRememberMe()) {
				accessLog.setAutoLoginOn(true);
				LoginSession entity = new LoginSession();
				entity.setLogin(login);
				int cookieMaxAge = 0;
				if (loginSessionExpiryAt == null) {
					cookieMaxAge = AppConstants.CK_AGE_30_DAYS * 3600;
					entity.setSessionExpiryAt(ZonedDateTime.now().plusDays(AppConstants.CK_AGE_30_DAYS));
				} else {
					ChronoUnit unit = ChronoUnit.SECONDS;
					cookieMaxAge = (int) unit.between(loginSessionExpiryAt, ZonedDateTime.now());
					entity.setSessionExpiryAt(loginSessionExpiryAt);
				}
				entity.setIpAddress(userIpAddress);
				entity.setDeviceInfo(loginBean.getUserAgent());
				entity.setLastAccessedAt(ZonedDateTime.now());
				loginSessionDao.insertLoginSession(entity);
				Cookie cookie = new Cookie(AppConstants.CK_AUTO_LOGIN,
						loginBean.getLoginId() + AppConstants.CK_SEPERATOR + entity.getSessionId());
				cookie.setSecure(true);
				cookie.setMaxAge(cookieMaxAge);
				loginBean.setCookie(cookie);
			}

			int logCount = (int) accessLogDao.getLogCountForUser(loginBean.getLoginId());
			if (logCount >= AppConstants.MAX_ACCESS_LOG) {
				int fetchCount = logCount - (AppConstants.MAX_ACCESS_LOG - 1);
				List<AccessLog> logList = accessLogDao.getExpiredLogForUser(loginBean.getLoginId(), fetchCount);
				logList.forEach(log -> accessLogDao.deleteAccessLog(log));
			}
			accessLogDao.insertAccessLog(accessLog);
			loginBean.setAccessLogId(accessLog.getLogId());
			loginBean.setSuccess(true);
		} catch (Exception e) {
			loginBean.setSuccess(false);
			String message = "System error occurred during login.";
			loginBean.setMessage(message);
			message += " Username: " + username;
			log.error(message, e);
		}
	}

	@Override
	public void execForgetPassword(LoginBean loginBean) {
		// TODO Auto-generated method stub
	}

	@Override
	@Transactional
	public boolean processLogout(SessionBean sessionBean) {
		long loginId = sessionBean.getLoginId();
		try {
			String sessionId = sessionBean.getAutoLoginId();
			if (sessionId != null)
				loginSessionDao.deleteLoginSession(sessionId);
			Login login = loginDao.getLoginDetails(loginId);
			login.setLastLoginAt(sessionBean.getLoginDatetime());
			loginDao.updateLoginDetails(login);
			AccessLog accessLog = accessLogDao.getAccessLogById(sessionBean.getAccessLogId());
			accessLog.setExpiredAt(Instant.now());
			accessLogDao.updateAccessLog(accessLog);
			log.info("Session invalidated by logout for Login ID: " + loginId + " & login ID: " + loginId);
			return true;
		} catch (Exception e) {
			String message = "System error occurred during logout.";
			message += " login ID: " + loginId;
			log.error(message, e);
			return false;
		}
	}
}
