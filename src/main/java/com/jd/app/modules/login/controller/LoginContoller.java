package com.jd.app.modules.login.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.jd.app.modules.SharedBean;
import com.jd.app.modules.login.bean.LoginBean;
import com.jd.app.modules.login.service.LoginService;
import com.jd.app.shared.annotation.GetJsonMapping;
import com.jd.app.shared.annotation.NoSessionCheck;
import com.jd.app.shared.annotation.PostJsonMapping;
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
		loginBean.setIpAddres(AppUtil.getClientIpAddress(request));
		loginBean.setUserAgent(request.getHeader("User-Agent"));
		loginService.authenticate(loginBean);

		if (loginBean.isSuccess()) {
			AppUtil.createNewSession(request, loginBean);
			if (loginBean.getCookie() != null)
				response.addCookie(loginBean.getCookie());
		}
		return loginBean;
	}

	@GetJsonMapping("/logout")
	public SharedBean logout(HttpServletRequest request, HttpServletResponse response) {
		return loginService.processLogout(request, response);
	}
}
