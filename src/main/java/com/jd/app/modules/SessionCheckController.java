package com.jd.app.modules;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.RestController;

import com.jd.app.shared.annotation.GetJsonMapping;
import com.jd.app.shared.annotation.NoSessionCheck;
import com.jd.app.shared.constant.general.AppConstants;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@NoSessionCheck
public class SessionCheckController {

	@GetJsonMapping("/cs")
	public SharedBean checkSession(HttpServletRequest request) {
		log.info("Session check initiated");
		HttpSession session = request.getSession(false);
		SharedBean bean = new SharedBean();
		boolean isValid = (session != null && // Check session validity
				session.getAttribute(AppConstants.SESSION_ATTR_USERNAME) != null);
		bean.setSuccess(isValid);
		log.info("Session check status: " + bean.isSuccess());
		return bean;
	}
}
