package com.jd.app.websocket.bean;

import com.jd.app.shared.constant.enums.CommType;
import com.jd.app.shared.constant.enums.ErrorLevel;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Joydeep Dey
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class WsError extends WsCommon {

	public WsError() {
		super.setCommType(CommType.ERR);
	}

	private String message;
	private ErrorLevel level;
}
