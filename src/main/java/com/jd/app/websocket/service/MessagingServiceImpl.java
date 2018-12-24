package com.jd.app.websocket.service;

import static com.jd.app.shared.constant.general.AppConstants.SESSION_ATTR_LOGIN_ID;
import static com.jd.app.shared.constant.general.AppConstants.SESSION_ATTR_USERNAME;
import static com.jd.app.websocket.bean.GlobalResource.ACTIVE_SESSION_HOLDER;
import static com.jd.app.websocket.bean.GlobalResource.OFFLINE_REQUEST_HOLDER;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jd.app.db.dao.def.LoginDao;
import com.jd.app.db.dao.def.MessageDao;
import com.jd.app.db.entity.Login;
import com.jd.app.db.entity.Message;
import com.jd.app.db.entity.User;
import com.jd.app.shared.constant.enums.AcknowledgeType;
import com.jd.app.shared.constant.enums.CommType;
import com.jd.app.shared.constant.enums.ErrorLevel;
import com.jd.app.shared.constant.enums.UserStatus;
import com.jd.app.shared.error.exceptions.DatabaseException;
import com.jd.app.shared.helper.AppUtil;
import com.jd.app.websocket.bean.WsAcknowledge;
import com.jd.app.websocket.bean.WsCommon;
import com.jd.app.websocket.bean.WsError;
import com.jd.app.websocket.bean.WsMessage;
import com.jd.app.websocket.bean.WsUserWrapper;

import lombok.extern.log4j.Log4j2;

/**
 * @author Joydeep Dey
 */
@Log4j2
@Service
public class MessagingServiceImpl implements MessagingService {

	@Autowired
	private ObjectMapper jsonMapper;

	@Autowired
	private LoginDao loginDao;

	@Autowired
	private MessageDao messageDao;

	private static final String UPLOAD_PATH = "C:/application/upload/";

	static {
		File f = new File(UPLOAD_PATH);
		if (!f.exists())
			f.mkdirs();
	}

	public void addUserToSession(WebSocketSession session) {
		String username = null;
		try {
			List<WebSocketSession> sessionList;
			username = session.getAttributes().get(SESSION_ATTR_USERNAME).toString();
			if (ACTIVE_SESSION_HOLDER.containsKey(username)) {
				sessionList = ACTIVE_SESSION_HOLDER.get(username);
			} else {
				sessionList = new ArrayList<>();
			}

			sessionList.add(session);
			ACTIVE_SESSION_HOLDER.put(username, sessionList);
			WsUserWrapper wrapper = new WsUserWrapper();
			wrapper.setSender(username);
			wrapper.setStatus(UserStatus.ONLINE);
			sendUserStatusUpdate(wrapper);

			log.info("WS Session added");
			log.info(MessageFormat.format("Username: {0}. Session ID: {1}. Total Active Sessions: {2}", username,
					session.getId(), sessionList.size()));

		} catch (Exception e) {
			log.error("Error occurred while adding user to session", e);
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public void removeUserFromSession(WebSocketSession session) {

		try {
			final String username = session.getAttributes().get(SESSION_ATTR_USERNAME).toString();
			boolean isSessionRemoved = false;

			List<WebSocketSession> sessionList = ACTIVE_SESSION_HOLDER.get(username);
			for (int i = 0; i < sessionList.size(); i++) {
				WebSocketSession targetSession = sessionList.get(i);
				if (targetSession.getId().equals(session.getId()) && !targetSession.isOpen()) {
					sessionList.remove(i);
					isSessionRemoved = true;
					break;
				}
			}
			if (isSessionRemoved && sessionList.isEmpty()) {
				ACTIVE_SESSION_HOLDER.remove(username);
				log.info("WS Session removed");
				log.info(MessageFormat.format("Username: {0}. Session ID: {1}. Total Active Sessions: {2}", username,
						session.getId(), sessionList.size()));

				// If an offline checking service is already executed, skip process
				if (OFFLINE_REQUEST_HOLDER.contains(username))
					return;

				long loginId = Long.parseLong(session.getAttributes().get(SESSION_ATTR_LOGIN_ID).toString());
				if (!checkForOffline(username, loginId))
					return;

				if (ACTIVE_SESSION_HOLDER.isEmpty()) {
					log.info("Skip sending notification as no more user is active. User" + username);
					return;
				}

				WsUserWrapper wsUser = new WsUserWrapper();
				wsUser.setSender(username);
				wsUser.setStatus(UserStatus.OFFLINE);
				wsUser.setLastOnlineDateTime(ZonedDateTime.now());
				sendUserStatusUpdate(wsUser);
			}
		} catch (

		Exception e) {
			log.error("Error occurred while removing user from session", e);
		}
	}

	@Transactional(rollbackFor = Exception.class)
	private boolean checkForOffline(String username, long loginId)
			throws InterruptedException, ExecutionException, DatabaseException {
		boolean sendOffline = false;
		OFFLINE_REQUEST_HOLDER.add(username);
		log.info("Initiating session check wait...user: " + username);
		Thread.sleep(1000 * 5L);
		OFFLINE_REQUEST_HOLDER.remove(username);
		if (!ACTIVE_SESSION_HOLDER.containsKey(username)) {
			sendOffline = true;
			log.info("No more session created in 15s. Updating last online. User: " + username);
			updateLastOnline(loginId);
		} else {
			log.info("Session created in 15s. Not updating last online. User: " + username);
		}
		return sendOffline;
	}

	@Transactional(rollbackFor = Exception.class)
	private void updateLastOnline(long loginId) throws DatabaseException {
		Login login = loginDao.getLoginDetails(loginId);
		login.setLastOnlineAt(ZonedDateTime.now());
		loginDao.updateLoginDetails(login);
	}

	@Transactional(rollbackFor = Exception.class)
	public WsCommon processMessage(WebSocketSession session, WebSocketMessage<?> message, short messageType)
			throws Exception {
		WsCommon wsCommon = new WsCommon();
		switch (messageType) {
		case MSG_TYPE_TEXT:
			wsCommon = processTextMessage((TextMessage) message);
			break;
		case MSG_TYPE_BIN:
			wsCommon = processBinaryMessage((BinaryMessage) message);
			break;
		case MSG_TYPE_PONG:
			wsCommon = processPongMessage((PongMessage) message);
			break;
		default:
			throw new Exception("Message type cannot be resolved.");
		}

		return wsCommon;
	}

	@Transactional(rollbackFor = Exception.class)
	public void sendMessage(WebSocketSession session, WsCommon wsCommon) throws Exception {
		String jsonMessage = null;
		TextMessage textMessage = null;

		if (wsCommon.getCommType() != CommType.ERR) {
			String[] receivers = wsCommon.getReceivers();
			for (String receiver : receivers) {
				String[] modReceivers = { receiver };
				wsCommon.setReceivers(modReceivers);
				jsonMessage = jsonMapper.writeValueAsString(wsCommon);
				textMessage = new TextMessage(jsonMessage);
				log.info("Message Content: " + jsonMessage + "\t Payload: " + textMessage.getPayload());
				List<WebSocketSession> sessionList = ACTIVE_SESSION_HOLDER.get(receiver);
				if (sessionList == null)
					continue;
				// Send message to all the active session of target
				for (WebSocketSession userSession : sessionList) {
					if (!userSession.isOpen())
						continue;
					log.info("Sending normal message");
					log.info("Destination: " + userSession);
					userSession.sendMessage(textMessage);
				}
			}
		}

		jsonMessage = jsonMapper.writeValueAsString(wsCommon);
		textMessage = new TextMessage(jsonMessage);
		// If error present, sent the error to current session holder
		if (wsCommon.getCommType() == CommType.ERR) {
			if (session.isOpen()) {
				log.info("Sending error message");
				log.info("Destination: " + session);
				session.sendMessage(textMessage);
			}
		} else if (wsCommon.getCommType() == CommType.MSG) {
			List<WebSocketSession> sessionList = ACTIVE_SESSION_HOLDER.get(wsCommon.getSender());
			// Send message to all the active session of the sender
			for (WebSocketSession userSession : sessionList) {
				if (!userSession.isOpen())
					continue;
				log.info("Sending normal message");
				log.info("Destination: " + userSession);
				userSession.sendMessage(textMessage);
			}
		}
	}

	@Transactional(rollbackFor = Exception.class)
	private WsCommon processTextMessage(TextMessage message) {
		WsCommon common = new WsCommon();
		byte[] bytes = message.asBytes();
		try {
			common = jsonMapper.readValue(bytes, WsCommon.class);
			switch (common.getCommType()) {
			case MSG:
				WsMessage wsMessage = new WsMessage();
				wsMessage = jsonMapper.readValue(bytes, WsMessage.class);
				saveMessage(wsMessage);
				return wsMessage;
			case ACK:
				WsAcknowledge wsAcknowledge = new WsAcknowledge();
				wsAcknowledge = jsonMapper.readValue(bytes, WsAcknowledge.class);
				updateMessageStatus(wsAcknowledge);
				return wsAcknowledge;
			case TYPE:
				return common;
			default:
				throw new Exception("CommType: " + common.getCommType() + " cannot be resolved.");
			}
		} catch (Exception e) {
			log.error(e);
			WsError wsError = new WsError();
			wsError.setLevel(ErrorLevel.LOW);
			wsError.setMessage("Failed to send the message.");
			return wsError;
		}
	}

	private static final String[] MEDIA_EXTS = { ".gif", ".png", ".jpg", ".jpeg" };
	private static final String[] DOCU_EXTS = { ".docx", ".doc", ".xls", "xlsx", ".txt", ".c", ".java", ".pdf" };

	@Transactional(rollbackFor = Exception.class)
	private WsCommon processBinaryMessage(BinaryMessage message) {
		try {
			WsMessage wsMessage = new WsMessage();
			ByteBuffer byteBuffer = message.getPayload();
			byte[] payloadBytes = byteBuffer.array();
			int bodyLen = Integer.parseInt(Byte.toString(payloadBytes[0]));
			log.info("Body length detected: " + bodyLen);
			byte[] body = new byte[bodyLen];
			for (int i = 1; i <= bodyLen; i++) {
				body[i - 1] = payloadBytes[i];
			}
			log.info("Message body: " + new String(body));
			wsMessage = jsonMapper.readValue(body, WsMessage.class);

			byte[] fileBytes = new byte[byteBuffer.limit() - bodyLen - 1];
			int idx = 0;
			for (int i = bodyLen + 1; i < payloadBytes.length; i++)
				fileBytes[idx++] = payloadBytes[i];

			String fileExt = wsMessage.getAvFile().substring(wsMessage.getAvFile().lastIndexOf("."));
			String filename = System.currentTimeMillis() + fileExt;
			boolean isAllowedMedia = isAllowedFile(fileExt, 0);
			if (isAllowedMedia) {
				wsMessage.setAvFile(filename);
			} else {
				boolean isAllowedDocument = isAllowedFile(fileExt, 1);
				if (isAllowedDocument)
					wsMessage.setDocFile(filename);
			}
			Files.write(Paths.get(UPLOAD_PATH, filename), fileBytes);
			saveMessage(wsMessage);
			return wsMessage;
		} catch (Exception e) {
			log.error("Failed to process the file.", e);
			WsError wsError = new WsError();
			wsError.setMessage("Failed to process the file.");
			return wsError;
		}
	}

	@Transactional(rollbackFor = Exception.class)
	private void saveMessage(WsMessage wsMessage) {
		Message message = new Message();
		message.setSender(new User(wsMessage.getSender()));
		message.setReceiver(new User(wsMessage.getReceivers()[0]));
		message.setContent(AppUtil.decodeText(wsMessage.getContent()));
		message.setAttachmentPathMedia(wsMessage.getAvFile());
		message.setAttachmentPathDoc(wsMessage.getDocFile());

		message.setSentAt(ZonedDateTime.now());
		message.setReceiverType(wsMessage.getReceiverType());
		messageDao.insertMessage(message);

		wsMessage.setMessageId(message.getMessageId());
		wsMessage.setSentAt(message.getSentAt());
	}

	@Transactional(rollbackFor = Exception.class)
	private void updateMessageStatus(WsAcknowledge wsAcknowledge) throws DatabaseException {
		List<Message> messages = null;
		String[] msgSenders = wsAcknowledge.getReceivers();
		long[] messageIds = wsAcknowledge.getMessageIds();

		if (messageIds != null && messageIds.length != 0)
			// When request comes from user click from list
			messages = messageDao.getMessagesById(wsAcknowledge.getMessageIds());
		else if (msgSenders != null && msgSenders.length != 0)
			// When request come from page load
			messages = messageDao.getMessagesForSenders(AcknowledgeType.SENT, wsAcknowledge.getSender(), msgSenders);
		else
			throw new IllegalArgumentException("Unsupported parameters for message status update");

		ZonedDateTime currentDateTime = ZonedDateTime.now();

		int mIdCount = 0;
		if (messageIds == null)
			messageIds = new long[messages.size()];
		String sMsgIds = StringUtils.join(messageIds, ',');
		if (messages.size() == 0)
			log.info("No message found by id(s): " + sMsgIds);
		else
			log.info(messages.size() + " message(s) found by id(s): " + sMsgIds);

		for (Message message : messages) {
			messageIds[mIdCount++] = message.getMessageId();
			if (wsAcknowledge.isRead()) {
				message.setDeliveredAt(currentDateTime);
				message.setReadAt(currentDateTime);
			} else {
				if (wsAcknowledge.isDelivered())
					message.setDeliveredAt(currentDateTime);
			}
			messageDao.updateMessage(message);
		}
		if (wsAcknowledge.isDelivered()) {
			wsAcknowledge.setDeliveredAt(currentDateTime);
		} else if (wsAcknowledge.isRead()) {
			wsAcknowledge.setDeliveredAt(currentDateTime);
			wsAcknowledge.setReadAt(currentDateTime);
		}
		if (wsAcknowledge.getMessageIds() == null)
			wsAcknowledge.setMessageIds(messageIds);
	}

	private static boolean isAllowedFile(String ext, int type) {
		String[] target = type == 0 ? MEDIA_EXTS : DOCU_EXTS;
		for (String aExt : target) {
			if (aExt.equals(ext))
				return true;
		}
		return false;
	}

	private static WsMessage processPongMessage(PongMessage message) {
		message.getPayload();
		return null;
	}

	private void sendUserStatusUpdate(WsUserWrapper wrapper) throws IOException {
		String userStatus = jsonMapper.writeValueAsString(wrapper);
		List<String> friendList = new ArrayList<>();
		List<WebSocketSession> sessionList = new ArrayList<>();
		ACTIVE_SESSION_HOLDER.entrySet().stream().filter(p -> !p.getKey().equals(wrapper.getSender()))
				.collect(Collectors.toConcurrentMap(Map.Entry::getKey, Map.Entry::getValue))
				.forEach((k, v) -> friendList.add(k));
		friendList.forEach(user -> {
			sessionList.addAll(ACTIVE_SESSION_HOLDER.get(user));
		});
		for (WebSocketSession session : sessionList) {
			if (session.isOpen())
				session.sendMessage(new TextMessage(userStatus));
		}
	}
}
