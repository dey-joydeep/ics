package com.jd.app.db.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.jd.app.db.dao.CommonDao;
import com.jd.app.db.dao.def.AccessLogDao;
import com.jd.app.db.entity.AccessLog;

/**
 * @author Joydeep Dey
 */
@Repository
public class AccessLogDaoImpl extends CommonDao implements AccessLogDao {

	private static final long serialVersionUID = -6789138514482687723L;

	public AccessLogDaoImpl() {
		setClass(AccessLog.class);
	}

	public void insertAccessLog(AccessLog accessLog) {
		create(accessLog);
	}

	public void updateAccessLog(AccessLog accessLog) {
		update(accessLog);
	}

	public void deleteAccessLog(AccessLog accessLog) {
		delete(accessLog);
	}

	public AccessLog getAccessLogById(long accessLogId) {
		return (AccessLog) findById(accessLogId);
	}

	public long getLogCountForUser(long loginId) {
		Map<String, Object> paramValueMap = new HashMap<>();
		String hql = "SELECT COUNT(al) FROM AccessLog al WHERE "
				+ "al.login.id = :loginId AND al.expiredAt IS NOT NULL";
		paramValueMap.put("loginId", loginId);
		Long count = (Long) findUnique(hql, paramValueMap);
		if (count == null)
			count = 0L;
		return count;
	}

	@SuppressWarnings("unchecked")
	public List<AccessLog> getExpiredLogForUser(long loginId, int count) {
		Map<String, Object> paramValueMap = new HashMap<>();
		String hql = "FROM AccessLog al WHERE al.login.id = :loginId AND al.expiredAt IS NOT NULL ORDER BY al.accessedAt";
		paramValueMap.put("loginId", loginId);
		return (List<AccessLog>) find(hql, paramValueMap, count);
	}
}
