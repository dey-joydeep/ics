package com.jd.app.modules.login.bean;

import javax.servlet.http.Cookie;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jd.app.modules.SharedBean;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Joydeep Dey
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
public class LoginBean extends SharedBean {

	private long loginId;

	@Getter(onMethod_ = @JsonIgnore)
	@Setter(onMethod_ = @JsonProperty)
	private String password;

	private boolean rememberMe;

	@JsonIgnore
	private String ipAddres;

	@JsonIgnore
	private String userAgent;;

	@JsonIgnore
	private String loginToken;

	@JsonIgnore
	private Cookie cookie;
}
