package com.jd.app.modules.chat.bean;

import com.jd.app.modules.SharedBean;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Joydeep Dey
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ChatBean extends SharedBean {

	private String receiver;
	private long[] messageIds;
}
