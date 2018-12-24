package com.jd.app.modules.login.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jd.app.modules.SharedBean;
import com.jd.app.modules.login.bean.LoginBean;

public interface LoginService {

	void authenticate(LoginBean loginBean);

	SharedBean processLogout(HttpServletRequest request, HttpServletResponse response);

	void execForgetPassword(LoginBean loginBean);
}
