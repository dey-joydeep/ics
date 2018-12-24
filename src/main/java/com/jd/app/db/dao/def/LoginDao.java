package com.jd.app.db.dao.def;

import com.jd.app.db.entity.Login;
import com.jd.app.shared.error.exceptions.DatabaseException;

/**
 * @author Joydeep Dey
 */
public interface LoginDao {

	Login getLoginDetails(long loginId);

	/**
	 * @param credential
	 */
	void insertLoginWithUser(Login login);

	/**
	 * @param credential
	 */
	void updateLoginDetails(Login login);

	/**
	 * Fetch the details of active user
	 * 
	 * @param loginId login ID of the user(username/email)
	 * @return Login entity
	 * @throws DatabaseException
	 */
	Login getLoginDetails(String loginId) throws DatabaseException;
}
