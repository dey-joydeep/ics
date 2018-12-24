package com.jd.app.websocket.bean;

import com.jd.app.shared.constant.enums.CommType;

import lombok.Data;

/**
 * @author Joydeep Dey
 */
@Data
public class WsCommon {

	private CommType commType;
	private String sender;
//	private String receiver;
	private String[] receivers;
}
