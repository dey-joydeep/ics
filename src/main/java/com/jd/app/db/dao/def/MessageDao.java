package com.jd.app.db.dao.def;

import java.util.List;

import com.jd.app.db.entity.Message;
import com.jd.app.shared.constant.enums.AcknowledgeType;
import com.jd.app.shared.error.exceptions.DatabaseException;

/**
 * @author Joydeep Dey
 */
public interface MessageDao {

	void insertMessage(Message message);

	void updateMessage(Message message);

	List<Message> getMessagesById(long[] messageIds) throws DatabaseException;

	Message getLastMessage(String sender, String receiver) throws DatabaseException;

	List<Message> getMessages(String sender, String receiver) throws DatabaseException;

	List<Message> getMessagesForSenders(AcknowledgeType status, String username, String... sender)
			throws DatabaseException;

	Long getUnreadMessagesCount(String sender, String receiver) throws DatabaseException;
}
