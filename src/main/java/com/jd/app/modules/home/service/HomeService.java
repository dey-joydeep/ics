package com.jd.app.modules.home.service;

import java.util.List;

import com.jd.app.modules.home.bean.MemberBean;
import com.jd.app.modules.home.bean.MessageBean;
import com.jd.app.modules.home.bean.UserBean;

public interface HomeService {

	UserBean getSelfDetails(String username);

	List<MemberBean> getChatHistory(String username);

	List<MessageBean> getMessages(String sender, String receiver);
}
