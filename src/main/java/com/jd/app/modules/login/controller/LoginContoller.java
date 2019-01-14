package com.jd.app.modules.login.controller;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.WebUtils;

import com.jd.app.modules.login.bean.LoginBean;
import com.jd.app.modules.login.service.LoginService;
import com.jd.app.shared.annotation.GetJsonMapping;
import com.jd.app.shared.annotation.NoSessionCheck;
import com.jd.app.shared.annotation.PostJsonMapping;
import com.jd.app.shared.constant.general.AppConstants;
import com.jd.app.shared.helper.AppUtil;

/**
 * @author Joydeep Dey
 */
@RestController
@NoSessionCheck
public class LoginContoller {

	@Autowired
	private LoginService loginService;

	@PostJsonMapping("/login")
	public LoginBean login(@RequestBody LoginBean loginBean, HttpServletRequest request, HttpServletResponse response) {
		loginBean.setIpAddress(AppUtil.getClientIpAddress(request));
		loginBean.setUserAgent(request.getHeader(AppConstants.REQ_HEADER_USER_AGENT));
		loginService.authenticate(loginBean);

		if (loginBean.isSuccess()) {
			AppUtil.createNewSession(request, loginBean);
			if (loginBean.getCookie() != null)
				response.addCookie(loginBean.getCookie());
		}
		return loginBean;
	}

	@GetJsonMapping("/logout")
	public void logout(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession(false);
		session.setAttribute(AppConstants.SESSION_ATTR_AUTO_EXPIRE, false);
		session.invalidate();
		Cookie autoLoginCookie = WebUtils.getCookie(request, AppConstants.CK_AUTO_LOGIN);
		if (autoLoginCookie == null)
			return;
		String[] cookieVals = autoLoginCookie.getValue().split(AppConstants.CK_SEPERATOR);
		if (cookieVals.length == 2) {
			autoLoginCookie.setMaxAge(0);
			response.addCookie(autoLoginCookie);
		}
	}
}
