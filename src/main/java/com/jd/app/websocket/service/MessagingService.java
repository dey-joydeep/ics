package com.jd.app.websocket.service;

import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import com.jd.app.shared.constant.enums.CommType;
import com.jd.app.shared.constant.enums.UserStatus;
import com.jd.app.websocket.bean.WebSocketSessionResource;
import com.jd.app.websocket.bean.WsCommon;

/**
 * The service interface for handling websocket session and incoming/outgoing
 * messages.
 * 
 * @author Joydeep Dey
 */
public interface MessagingService {

	/** Text Message */
	static final short MSG_TYPE_TEXT = 0x0;
	/** Binary Message */
	static final short MSG_TYPE_BIN = 0x1;
	/** Pong Message */
	static final short MSG_TYPE_PONG = 0x2;

	/**
	 * Add an logged in user to websocket session holder.<br>
	 * If the user is already added from some different session, get the existing
	 * list by <code>username</code> and add newly created session to that list. If
	 * the user is not present in the session holder map, add a new entry being
	 * <code>username</code> as the key and send <code>online</code>(see:
	 * {@link UserStatus}) notification to other members, associated with this user.
	 * 
	 * @param session The newly created websocket session
	 */
	public void addUserToSession(WebSocketSession session);

	/**
	 * Remove an user session from the web socket session holder.<br>
	 * Find the session list from {@link WebSocketSessionResource}'s
	 * <code>ACTIVE_SESSION_HOLDER</code> and remove the target session. <br>
	 * <br>
	 * If the session removal is successful and the list become empty, remove the
	 * user from <code>ACTIVE_SESSION_HOLDER</code> itself. Also, send an
	 * <code>offline</code> notification to the members associated with this user
	 * and update last online date-time to the database.
	 * 
	 * @param session The session to be removed
	 */
	public void removeUserFromSession(WebSocketSession session);

	/**
	 * Process the incoming messages(see: {@link WebSocketMessage}) based on their
	 * type, i.e. text, binary or pong and return the processed result as
	 * {@link WsCommon} for sending to the sender and receiver or only to sender
	 * depending on the {@link CommType}
	 * 
	 * @param session     Sender's websocket session
	 * @param message     websocket message to be processed
	 * @param messageType Type of the message (text/binary/pong)
	 * @return Processed result
	 * @throws Exception If any error occurs
	 */
	WsCommon processMessage(WebSocketSession session, WebSocketMessage<?> message, short messageType) throws Exception;

	/**
	 * Send message to the destination depending on the {@link CommType}.<br>
	 * <br>
	 * <table border="1">
	 * <tr>
	 * <th>CommType</th>
	 * <th>Destination</th>
	 * <th>Session</th>
	 * </tr>
	 * <tbody>
	 * <tr>
	 * <td>ERR</td>
	 * <td>Sender</td>
	 * <td>Current</td>
	 * </tr>
	 * <tr>
	 * <td>MSG</td>
	 * <td>Sender, Receiver</td>
	 * <td>All</td>
	 * </tr>
	 * <tr>
	 * <td>ACK &amp; TYPE</td>
	 * <td>Receiver</td>
	 * <td>All</td>
	 * </tr>
	 * <tr>
	 * <td>NOT</td>
	 * <td>All associated members</td>
	 * <td>All</td>
	 * </tr>
	 * </tbody>
	 * </table>
	 * 
	 * @param session  Message sender's websocket session
	 * @param wsCommon Message to be sent
	 * @throws Exception If any error occurs
	 */
	public void sendMessage(WebSocketSession session, WsCommon wsCommon) throws Exception;
}
