package com.jd.app.db.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Repository;

import com.jd.app.db.dao.CommonDao;
import com.jd.app.db.dao.def.MessageDao;
import com.jd.app.db.entity.Message;
import com.jd.app.shared.constant.enums.AcknowledgeType;
import com.jd.app.shared.error.exceptions.DatabaseException;

@Repository
public class MessageDaoImpl extends CommonDao implements MessageDao {

	private static final long serialVersionUID = 1L;

	public MessageDaoImpl() {
		setClass(Message.class);
	}

	public void insertMessage(Message message) {
		create(message);
	}

	public void updateMessage(Message message) {
		update(message);
	}

	public List<Message> getMessagesById(long[] messageIds) throws DatabaseException {
		try {
			if (messageIds == null || messageIds.length == 0)
				throw new IllegalArgumentException(
						"Inappropriate argument list [messageIds]. At least one is required.");
			Map<String, Object> paramValueMap = new HashMap<>();
			String hql = "FROM Message m WHERE m.messageId ";
			if (messageIds.length == 1) {
				hql += "= :messageId";
				paramValueMap.put("messageId", messageIds[0]);
			} else {
				hql += "IN (:messageIds)";
				paramValueMap.put("messageIds", List.of(ArrayUtils.toObject(messageIds)));
			}
			@SuppressWarnings("unchecked")
			List<Message> messages = (List<Message>) find(hql, paramValueMap);
			return messages;
		} catch (Exception e) {
			throw new DatabaseException("Error occurred during message fetch", e);
		}
	}

	public Message getLastMessage(String sender, String receiver) throws DatabaseException {
		try {
			List<Message> messages = getMessages(sender, receiver, 1);
			return messages.size() > 0 ? messages.get(0) : null;
		} catch (Exception e) {
			throw new DatabaseException("Error occurred during message fetch", e);
		}
	}

	public List<Message> getMessages(String sender, String receiver) throws DatabaseException {
		try {
			return getMessages(sender, receiver, 0);
		} catch (Exception e) {
			throw new DatabaseException("Error occurred during message fetch", e);
		}
	}

	@SuppressWarnings("unchecked")
	private List<Message> getMessages(String sender, String receiver, int count) {
		String hql = "FROM Message m WHERE ((m.sender.username = :sender and m.receiver.username = :receiver) OR "
				+ "(m.receiver.username = :sender and m.sender.username = :receiver)) ORDER BY m.sentAt DESC";
		Map<String, Object> paramValueMap = new HashMap<>();
		paramValueMap.put("sender", sender);
		paramValueMap.put("receiver", receiver);
		return (List<Message>) find(hql, paramValueMap, count);
	}

	@SuppressWarnings("unchecked")
	public List<Message> getMessagesForSenders(AcknowledgeType status, String username, String... senders)
			throws DatabaseException {
		try {
			Map<String, Object> paramValueMap = new HashMap<>();
			String hql = "FROM Message m WHERE m.receiver.username = :username ";
			if (senders != null) {
				if (senders.length == 1) {
					hql += "AND m.sender.username = :sender ";
					paramValueMap.put("sender", senders[0]);
				} else {
					hql += "AND m.sender.username IN (:sender) ";
					paramValueMap.put("sender", List.of(senders));
				}
			}
			if (AcknowledgeType.SENT == status) {
				hql += "AND m.readAt IS NULL AND m.deliveredAt IS NULL";
			}
			if (AcknowledgeType.DELIVERED == status) {
				hql += "AND m.deliveredAt IS NULL";
			}
			if (AcknowledgeType.READ == status) {
				hql += "AND m.readAt IS NULL";
			}
			paramValueMap.put("username", username);
			return (List<Message>) find(hql, paramValueMap);
		} catch (Exception e) {
			throw new DatabaseException("Error occurred during message fetch", e);
		}
	}

	public Long getUnreadMessagesCount(String sender, String receiver) throws DatabaseException {
		try {
			Map<String, Object> paramValueMap = new HashMap<>();
			String hql = "SELECT COUNT(m) FROM Message m WHERE m.receiver.username = :receiver AND m.sender.username = :sender AND (m.readAt IS NULL OR m.deliveredAt IS NULL)";
			paramValueMap.put("sender", sender);
			paramValueMap.put("receiver", receiver);
			Long count = (Long) findUnique(hql, paramValueMap);
			if (count == null)
				count = 0L;
			return count;
		} catch (Exception e) {
			throw new DatabaseException("Error occurred during message fetch", e);
		}
	}
}
