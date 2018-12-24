package com.jd.app.websocket.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import com.jd.app.websocket.bean.WsCommon;
import com.jd.app.websocket.service.MessagingService;

import lombok.extern.log4j.Log4j2;

/**
 * @author Joydeep Dey
 */
@Log4j2
@Component
public class MessageHandler extends AbstractWebSocketHandler {

	@Autowired
	private MessagingService messagingService;

	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		messagingService.addUserToSession(session);
		log.info("Websocket connection establised");
	}

	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		log.info("Connection closed.\nReason:" + status.getReason());
		messagingService.removeUserFromSession(session);
	}

	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		WsCommon wsCommon = messagingService.processMessage(session, message, MessagingService.MSG_TYPE_TEXT);
		messagingService.sendMessage(session, wsCommon);
	}

	protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
		WsCommon wsCommon = messagingService.processMessage(session, message, MessagingService.MSG_TYPE_BIN);
		messagingService.sendMessage(session, wsCommon);
	}

	protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
		WsCommon wsCommon = messagingService.processMessage(session, message, MessagingService.MSG_TYPE_PONG);
		messagingService.sendMessage(session, wsCommon);
	}

	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		exception.printStackTrace();
	}

}
