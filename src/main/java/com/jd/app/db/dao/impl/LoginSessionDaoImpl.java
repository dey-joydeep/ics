package com.jd.app.db.dao.impl;

import java.util.TreeMap;

import org.springframework.stereotype.Repository;

import com.jd.app.db.dao.CommonDao;
import com.jd.app.db.dao.def.LoginSessionDao;
import com.jd.app.db.entity.LoginSession;
import com.jd.app.shared.error.exceptions.DatabaseException;

/**
 * @author Joydeep Dey
 */
@Repository
public class LoginSessionDaoImpl extends CommonDao implements LoginSessionDao {

	private static final long serialVersionUID = 1L;

	public LoginSessionDaoImpl() {
		setClass(LoginSession.class);
	}

	public LoginSession getLoginSessionDetails(LoginSession loginSession) throws DatabaseException {
		try {
			String hql = "FROM LoginSession ls WHERE ls.sessionId = :sessionId "
					+ "AND ls.ipAddress = :ipAddress AND ls.login.id = :loginId AND ls.login.active IS TRUE";
			TreeMap<String, Object> paramValueMap = new TreeMap<>();
			paramValueMap.put("sessionId", loginSession.getSessionId());
			paramValueMap.put("ipAddress", loginSession.getIpAddress());
			paramValueMap.put("loginId", loginSession.getLogin().getId());
			LoginSession result = (LoginSession) findUnique(hql, paramValueMap);
			return result;
		} catch (Exception e) {
			throw new DatabaseException("Failed to fetch login session details", e);
		}
	}

	public void deleteLoginSession(String sessionId) throws DatabaseException {
		deleteById(sessionId);
	}

	public void insertLoginSession(LoginSession loginSession) throws DatabaseException {
		create(loginSession);
	}
}
