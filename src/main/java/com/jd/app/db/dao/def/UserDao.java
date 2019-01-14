package com.jd.app.db.dao.def;

import java.util.List;

import com.jd.app.db.entity.User;
import com.jd.app.db.entity.rst.ChatHistory;
import com.jd.app.shared.error.exceptions.DatabaseException;

/**
 * User DAO interface
 * 
 * @author Joydeep Dey
 */
public interface UserDao {

	/**
	 * @param user
	 * @throws DatabaseException
	 */
	void insertSignupData(User user) throws DatabaseException;

	/**
	 * @param user
	 * @throws DatabaseException
	 */
	void updateUser(User user) throws DatabaseException;

	/**
	 * @param username
	 * @return username existence result
	 * @throws DatabaseException
	 */
	boolean isExistingUsername(String username) throws DatabaseException;

	/**
	 * Fetch the details of active user
	 * 
	 * @param username Username of the user to be fetched
	 * @return User entity
	 * @throws DatabaseException
	 */
	User getUserByUsername(String username) throws DatabaseException;

	List<ChatHistory> getChatHistoryList(String username) throws DatabaseException;
}