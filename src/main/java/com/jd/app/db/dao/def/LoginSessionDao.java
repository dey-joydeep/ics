package com.jd.app.db.dao.def;

import com.jd.app.db.entity.LoginSession;
import com.jd.app.shared.error.exceptions.DatabaseException;

/**
 * @author Joydeep Dey
 */
public interface LoginSessionDao {

	public LoginSession getLoginSessionDetails(LoginSession loginSession) throws DatabaseException;

	public void deleteLoginSession(String sessionId) throws DatabaseException;

	public void insertLoginSession(LoginSession loginSession) throws DatabaseException;
}
