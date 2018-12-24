/**
 * 
 */
package com.jd.app.websocket.service;

import java.io.IOException;

import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import com.jd.app.websocket.bean.WsCommon;

/**
 * @author Joydeep Dey
 */
public interface MessagingService {

	/***/
	static final short MSG_TYPE_TEXT = 0x0;
	/***/
	static final short MSG_TYPE_BIN = 0x1;
	/***/
	static final short MSG_TYPE_PONG = 0x2;

	/**
	 * @param session
	 */
	public void addUserToSession(WebSocketSession session);

	/**
	 * @param session
	 */
	public void removeUserFromSession(WebSocketSession session);

	/**
	 * 
	 * @param session
	 * @param message
	 * @param messageType
	 * @return
	 * @throws Exception
	 */
	WsCommon processMessage(WebSocketSession session, WebSocketMessage<?> message, short messageType) throws Exception;

	/**
	 * 
	 * @param session
	 * @param wsCommon
	 * @throws IOException
	 * @throws Exception
	 */
	public void sendMessage(WebSocketSession session, WsCommon wsCommon) throws IOException, Exception;
}
