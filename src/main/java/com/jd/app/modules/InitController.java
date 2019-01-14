package com.jd.app.modules;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.WebUtils;

import com.jd.app.modules.login.bean.LoginBean;
import com.jd.app.modules.login.service.LoginService;
import com.jd.app.shared.constant.general.AppConstants;
import com.jd.app.shared.helper.AppUtil;

import lombok.extern.log4j.Log4j2;

/**
 * @author Joydeep Dey
 */
@Log4j2
@Controller
public class InitController {

	@Autowired
	private LoginService loginService;

	@RequestMapping("/")
	public String initApp(HttpServletRequest request, HttpServletResponse response) {
		boolean isValid = false;
		Object ob = request.getAttribute(AppConstants.REQ_ATTR_USER_VALID);
		if (ob != null) {
			isValid = Boolean.parseBoolean(ob.toString());
		}

		if (isValid) {
			log.info("Valid session found. Redirecting to home page.");
			return "home";
		}

		log.info("Valid session not found. Trying to auto-login.");
		LoginBean loginBean = new LoginBean();
		// Look for session cookie- loginToken:_al
		Cookie loginCookie = WebUtils.getCookie(request, AppConstants.CK_AUTO_LOGIN);
		if (loginCookie != null) {
			loginBean.setIpAddress(AppUtil.getClientIpAddress(request));
			loginBean.setLoginToken(loginCookie.getValue());

			loginBean.setUserAgent(request.getHeader(AppConstants.REQ_HEADER_USER_AGENT));
			loginService.authenticate(loginBean);

			if (loginBean.isSuccess()) {
				AppUtil.createNewSession(request, loginBean);
				if (loginBean.getCookie() != null)
					response.addCookie(loginBean.getCookie());
				return "home";
			}
		} else {
			log.info("Auto-login information not found.");
		}

		log.info("Valid session not found. Redirecting to login page.");
		return "login";
	}
}
