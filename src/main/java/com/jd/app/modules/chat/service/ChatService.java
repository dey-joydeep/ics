package com.jd.app.modules.chat.service;

import com.jd.app.modules.chat.bean.ChatBean;

/**
 * @author Joydeep Dey
 */
public interface ChatService {

	void archive(ChatBean chatBean);

	void delete(ChatBean chatBean);
}
