package com.jd.app.db.entity.rst;

import java.util.List;

import org.hibernate.transform.ResultTransformer;

import com.jd.app.db.entity.Message;
import com.jd.app.db.entity.User;

/**
 * @author Joydeep Dey
 */
public class ChatHistoryResultTransformer implements ResultTransformer {

	private static final long serialVersionUID = -1324724910253741348L;

	public Object transformTuple(Object[] tuple, String[] aliases) {
		return new ChatHistory((User) tuple[0], (Message) tuple[1]);
	}

	@SuppressWarnings("rawtypes")
	public List transformList(List collection) {
		return collection;
	}
}
