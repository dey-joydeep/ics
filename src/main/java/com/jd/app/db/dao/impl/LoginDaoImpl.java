package com.jd.app.db.dao.impl;

import java.time.Instant;
import java.util.TreeMap;

import org.springframework.stereotype.Repository;

import com.jd.app.db.dao.CommonDao;
import com.jd.app.db.dao.def.LoginDao;
import com.jd.app.db.entity.Login;
import com.jd.app.shared.error.exceptions.DatabaseException;

import lombok.extern.log4j.Log4j2;

/**
 * @author Joydeep Dey
 */
@Log4j2
@Repository
public class LoginDaoImpl extends CommonDao implements LoginDao {

	private static final long serialVersionUID = -6157743926725977815L;

	public LoginDaoImpl() {
		setClass(Login.class);
	}

	public Login getLoginDetails(long loginId) {
		return (Login) findById(loginId);
	}

	public void insertLoginWithUser(Login login) {
		try {
			login.setActive(true);
			Instant now = Instant.now();
			login.setCreatedAt(now);
			login.setUpdatedAt(now);
			create(login);
		} catch (Exception e) {
			log.error("Could not write data into User table: \n" + login.toString(), e);
		}
	}

	public void updateLoginDetails(Login Login) {
		Login.setUpdatedAt(Instant.now());
		update(Login);
	}

	public Login getLoginDetails(String loginId) throws DatabaseException {
		try {
			String hql = "SELECT l FROM Login l WHERE (l.email = :loginId OR l.user.username= :loginId) AND l.active IS TRUE";
			TreeMap<String, Object> paramValueMap = new TreeMap<>();
			paramValueMap.put("loginId", loginId);
			Login login = (Login) findUnique(hql, paramValueMap);
			return login == null ? null : login;
		} catch (Exception e) {
			throw new DatabaseException("Failed to fetch login details", e);
		}
	}
}
