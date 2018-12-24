package com.jd.app.modules.home.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.WebUtils;

import com.jd.app.modules.home.bean.MemberBean;
import com.jd.app.modules.home.bean.MessageBean;
import com.jd.app.modules.home.bean.UserBean;
import com.jd.app.modules.home.service.HomeService;
import com.jd.app.shared.annotation.GetJsonMapping;
import com.jd.app.shared.constant.general.AppConstants;

/**
 * @author Joydeep Dey
 */
@RestController
@RequestMapping("/load")
public class HomeController {

	@Autowired
	private HomeService homeService;

	@GetMapping("/selfdetails")
	public UserBean getUsername(HttpServletRequest request) {
		Object ob = WebUtils.getSessionAttribute(request, AppConstants.SESSION_ATTR_USERNAME);
		String username = ob != null ? ob.toString() : "";
		return homeService.getSelfDetails(username);
	}

	@GetJsonMapping("/member")
	public List<MemberBean> loadTeamMembers(@RequestParam String username) {
		return homeService.getMembers(username);
	}

	@GetJsonMapping("/message")
	public List<MessageBean> loadMessages(@RequestParam String sender, @RequestParam String receiver) {
		return homeService.getMessages(sender, receiver);
	}
}
