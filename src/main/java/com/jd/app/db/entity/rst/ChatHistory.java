package com.jd.app.db.entity.rst;

import com.jd.app.db.entity.Message;
import com.jd.app.db.entity.User;
import com.jd.app.db.entity.common.CommonEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Joydeep Dey
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ChatHistory extends CommonEntity {
	private static final long serialVersionUID = 979507253529673072L;
	private final User user;
	private final Message message;
}
