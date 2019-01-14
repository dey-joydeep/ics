package com.jd.app.modules.profile.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import com.jd.app.modules.profile.bean.ProfileBean;
import com.jd.app.shared.annotation.PostJsonMapping;

/**
 * @author Joydeep Dey
 */
@RestController
@RequestMapping("/profile")
public class ProfileController {

	@PostJsonMapping("/update/info")
	public ProfileBean updateInfo(@RequestBody ProfileBean profileBean) {
		return profileBean;
	}

	@PostJsonMapping("/update/photo")
	public ProfileBean updatePhoto(@RequestBody ProfileBean profileBean) {
		return profileBean;
	}
}
