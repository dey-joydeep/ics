package com.jd.app.modules.chat.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jd.app.db.dao.def.MessageDao;
import com.jd.app.db.entity.Message;
import com.jd.app.modules.chat.bean.ChatBean;
import com.jd.app.shared.error.exceptions.DatabaseException;

import lombok.extern.log4j.Log4j2;

/**
 * @author Joydeep Dey
 */
@Log4j2
@Service
public class ChatServiceImpl implements ChatService {

	@Autowired
	private MessageDao messageDao;

	public void archive(ChatBean chatBean) {
	}

	@Transactional(rollbackFor = Exception.class)
	public void delete(ChatBean chatBean) {
		try {
			List<Message> messages = messageDao.getMessagesById(chatBean.getMessageIds());
			messages.forEach(message -> {
				if (chatBean.getUsername().equals(message.getSender().getUsername()))
					message.setSenderDelete(true);
				else
					message.setReceiverDelete(true);
				messageDao.updateMessage(message);
			});
			chatBean.setSuccess(true);
		} catch (DatabaseException e) {
			chatBean.setMessage("Failed to delete selected messages");
			log.error(e);
		}
	}
}
