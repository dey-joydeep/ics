package com.jd.app.shared.helper;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.jd.app.modules.login.bean.LoginBean;

public class FormValidator {

	public static final Map<String, String> validateLoginForm(LoginBean loginBean) {
		String userId = loginBean.getUsername();
		String password = loginBean.getPassword();
		Map<String, String> errors = new HashMap<>();
		if (StringUtils.isBlank(userId))
			errors.put("username", "required");

		if (StringUtils.isBlank(password))
			errors.put("password", "required");
		return errors;
	}
}
