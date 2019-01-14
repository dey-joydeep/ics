package com.jd.app.websocket.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
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
		log.info("Connection closed.\nReason:" + status.getCode());
		messagingService.removeUserFromSession(session);
	}

	@Override
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
		short messageType;
		if (message instanceof TextMessage) {
			messageType = MessagingService.MSG_TYPE_TEXT;
		} else if (message instanceof BinaryMessage) {
			messageType = MessagingService.MSG_TYPE_BIN;
		} else if (message instanceof PongMessage) {
			messageType = MessagingService.MSG_TYPE_PONG;
		} else {
			throw new IllegalStateException("Unexpected WebSocket message type: " + message);
		}

		// Process the received message and store in DB
		WsCommon wsCommon = messagingService.processMessage(session, message, messageType);

		// Send the message to destination
		messagingService.sendMessage(session, wsCommon);
	}

	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		exception.printStackTrace();
	}
}
