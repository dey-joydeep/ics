package com.jd.app.db.dao.impl;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Repository;

import com.jd.app.db.dao.CommonDao;
import com.jd.app.db.dao.def.UserDao;
import com.jd.app.db.entity.Login;
import com.jd.app.db.entity.User;
import com.jd.app.db.entity.rst.ChatHistory;
import com.jd.app.db.entity.rst.ChatHistoryResultTransformer;
import com.jd.app.shared.error.exceptions.DatabaseException;

/**
 * @author Joydeep Dey
 */
@Repository
public class UserDaoImpl extends CommonDao implements UserDao {

	private static final long serialVersionUID = -3665023914005000010L;

	public UserDaoImpl() {
		setClass(User.class);
	}

	@Override
	public void insertSignupData(User user) throws DatabaseException {
		try {
			Instant now = Instant.now();

			user.setCreatedAt(now);
			user.setUpdatedAt(now);

			Login login = user.getLogin();
			login.setActive(true);
			login.setCreatedAt(now);
			login.setUpdatedAt(now);
			super.create(user);
		} catch (Exception e) {
			throw new DatabaseException("Could not write data into User table: \n" + user.toString(), e);
		}
	}

	public void updateUser(User user) throws DatabaseException {
		try {
			user.setUpdatedAt(Instant.now());
			update(user);
		} catch (Exception e) {
			throw new DatabaseException("Failed to update user info", e);
		}
	}

	public boolean isExistingUsername(String username) throws DatabaseException {
		try {
			String hql = "SELECT COUNT(u) FROM User u WHERE u.username= :username AND u.active IS TRUE";
			TreeMap<String, Object> paramValueMap = new TreeMap<>();
			paramValueMap.put("username", username);
			return ((long) findUnique(hql, paramValueMap) > 0);
		} catch (Exception e) {
			throw new DatabaseException("Could not check username existence", e);
		}
	}

	public User getUserByUsername(String username) throws DatabaseException {
		try {
			return (User) findById(username);
		} catch (Exception e) {
			throw new DatabaseException("Could not check username existence", e);
		}
	}

	@SuppressWarnings("unchecked")
	public List<ChatHistory> getChatHistoryList(String username) throws DatabaseException {
		try {
			String hql = "FROM User u INNER JOIN FETCH u.login l LEFT JOIN FETCH Message m ON "
					+ "((m.sender.username = u.username AND m.receiver.username = :username AND m.receiverDelete IS FALSE) "
					+ "OR (m.receiver.username = u.username AND m.sender.username = :username AND m.senderDelete IS FALSE)) "
					+ "AND m.messageId = (SELECT MAX(msg.messageId) FROM Message msg WHERE"
					+ "((msg.sender , msg.receiver) = (m.sender , m.receiver) AND msg.senderDelete IS FALSE)"
					+ "OR ((msg.sender , msg.receiver) = (m.receiver , m.sender) AND m.receiverDelete IS FALSE)) "
					+ "WHERE l.active IS TRUE AND u.username != :username ORDER BY m.sentAt DESC";
			Map<String, Object> paramValueMap = new HashMap<>();
			paramValueMap.put("username", username);
			setResultTransformer(new ChatHistoryResultTransformer());
			return (List<ChatHistory>) find(hql, paramValueMap);
		} catch (Exception e) {
			throw new DatabaseException("Could not load chat history", e);
		}
	}
}
