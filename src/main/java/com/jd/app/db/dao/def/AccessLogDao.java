package com.jd.app.db.dao.def;

import java.util.List;

import com.jd.app.db.entity.AccessLog;

/**
 * @author Joydeep Dey
 */
public interface AccessLogDao {

	/**
	 * @param accessLog
	 */
	void insertAccessLog(AccessLog accessLog);

	/**
	 * @param accessLog
	 */
	void updateAccessLog(AccessLog accessLog);

	/**
	 * 
	 * @param accessLog
	 */
	void deleteAccessLog(AccessLog accessLog);

	/**
	 * 
	 * @param accessLogId
	 * @return access log entity
	 */
	AccessLog getAccessLogById(long accessLogId);

	/**
	 * 
	 * @param loginId
	 * @return log count
	 */
	long getLogCountForUser(long loginId);

	/**
	 * 
	 * @param loginId
	 * @param count
	 * @return expired access logs
	 */
	List<AccessLog> getExpiredLogForUser(long loginId, int count);
}
