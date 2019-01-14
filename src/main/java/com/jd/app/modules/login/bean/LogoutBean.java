package com.jd.app.modules.login.bean;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jd.app.modules.SharedBean;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Joydeep Dey
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class LogoutBean extends SharedBean {

	@JsonIgnore
	private long loginId;
	@JsonIgnore
	private long accessLogId;
	@JsonIgnore
	private String autoLoginSessionId;
	@JsonIgnore
	private ZonedDateTime loginDatetime;
}
