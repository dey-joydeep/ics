package com.jd.app.modules;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

/**
 * @author Joydeep Dey
 */
@Data
public class SharedBean {

	private String username;
	private String selfFirstName;
	private boolean success;
	private String message;

	@JsonIgnore
	private Object[] messageParams;

	private Map<String, String> errors;

	public void setMessageParams(Object... messageParams) {
		this.messageParams = messageParams;
	}
}
