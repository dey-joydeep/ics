package com.jd.app.modules.login.service;

import com.jd.app.modules.login.bean.LoginBean;
import com.jd.app.shared.helper.SessionBean;

public interface LoginService {

	void authenticate(LoginBean loginBean);

	boolean processLogout(SessionBean sessionBean);

	void execForgetPassword(LoginBean loginBean);
}
