package com.jd.app.websocket.service;

import static com.jd.app.shared.constant.general.AppConstants.SESSION_ATTR_LOGIN_ID;
import static com.jd.app.shared.constant.general.AppConstants.SESSION_ATTR_USERNAME;
import static com.jd.app.websocket.bean.WebSocketSessionResource.ACTIVE_SESSION_HOLDER;
import static com.jd.app.websocket.bean.WebSocketSessionResource.OFFLINE_REQUEST_HOLDER;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
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
import com.jd.app.shared.constant.enums.AnswerType;
import com.jd.app.shared.constant.enums.CommType;
import com.jd.app.shared.constant.enums.ContentType;
import com.jd.app.shared.constant.enums.ErrorLevel;
import com.jd.app.shared.constant.enums.MediaType;
import com.jd.app.shared.constant.enums.UserStatus;
import com.jd.app.shared.constant.general.AppConstants;
import com.jd.app.shared.error.exceptions.DatabaseException;
import com.jd.app.shared.helper.AppUtil;
import com.jd.app.websocket.bean.WsAcknowledge;
import com.jd.app.websocket.bean.WsCommon;
import com.jd.app.websocket.bean.WsError;
import com.jd.app.websocket.bean.WsMessage;
import com.jd.app.websocket.bean.WsUserWrapper;

import lombok.extern.log4j.Log4j2;

/**
 * The implementation class of MessagingService interface.
 * 
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
			boolean isSessionRemoved = false;
			final String username = session.getAttributes().get(SESSION_ATTR_USERNAME).toString();

			List<WebSocketSession> sessionList = ACTIVE_SESSION_HOLDER.get(username);
			Iterator<WebSocketSession> sessionIterator = sessionList.iterator();
			while (sessionIterator.hasNext()) {
				WebSocketSession targetSession = sessionIterator.next();
				if (targetSession.getId().equals(session.getId()) && !targetSession.isOpen()) {
					sessionIterator.remove();
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
	private boolean checkForOffline(String username, long loginId) throws InterruptedException {
		OFFLINE_REQUEST_HOLDER.add(username);
		log.info("Initiating session check wait...user: " + username);
		Thread.sleep(1000 * 5L);
		OFFLINE_REQUEST_HOLDER.remove(username);
		if (!ACTIVE_SESSION_HOLDER.containsKey(username)) {
			log.info("No more session created in 5s. Updating last online. User: " + username);
			updateLastOnline(loginId);
			return true;
		}
		log.info("Session created in 5s. Not updating last online. User: " + username);
		return false;
	}

	@Transactional(rollbackFor = Exception.class)
	private void updateLastOnline(long loginId) {
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
					changeTimeZone(userSession);
					userSession.sendMessage(textMessage);
				}
			}
		}

		jsonMessage = jsonMapper.writeValueAsString(wsCommon);
		textMessage = new TextMessage(jsonMessage);
		// If error present, sent the error to current session holder
		if (wsCommon.getCommType() == CommType.ERR && session.isOpen()) {
			log.info("Sending error message");
			log.info("Destination: " + session);
			session.sendMessage(textMessage);
			return;
		}
		if (wsCommon.getCommType() == CommType.MSG) {
			List<WebSocketSession> sessionList = ACTIVE_SESSION_HOLDER.get(wsCommon.getSender());
			// Send message to all the active session of the sender
			for (WebSocketSession userSession : sessionList) {
				if (!userSession.isOpen())
					continue;
				log.info("Sending normal message");
				log.info("Destination: " + userSession);
				changeTimeZone(userSession);
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
				wsMessage.setContentType(ContentType.TEXT);
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

	@Transactional(rollbackFor = Exception.class)
	private WsCommon processBinaryMessage(BinaryMessage message) {
		try {
			WsMessage wsMessage = new WsMessage();
			ByteBuffer byteBuffer = message.getPayload();
			/*
			 * byte[] payloadBytes = byteBuffer.array(); int bodyLen =
			 * Integer.parseInt(Byte.toString(payloadBytes[0]));
			 * log.info("Body length detected: " + bodyLen); byte[] body = new
			 * byte[bodyLen]; for (int i = 1; i <= bodyLen; i++) { body[i - 1] =
			 * payloadBytes[i]; }
			 */
			// New Content start
			String messageContent = new String(byteBuffer.array(), StandardCharsets.UTF_8);
			int sepIdx = messageContent.indexOf('\n');
			String body = messageContent.substring(0, sepIdx);
			String base64Content = messageContent.substring(sepIdx + 1);
			String fileBase64 = base64Content.substring(base64Content.indexOf(',') + 1);
			byte[] fileBytes = Base64.getDecoder().decode(fileBase64.getBytes(StandardCharsets.UTF_8));
			// New Content end

			log.info("Message body: " + new String(body));
			wsMessage = jsonMapper.readValue(body, WsMessage.class);

			String mainFileName = wsMessage.getMainFilename();
			int dotIdx = mainFileName.lastIndexOf(".");
			String fileExt = null;
			if (dotIdx != -1)
				fileExt = mainFileName.substring(dotIdx);

			if (dotIdx == -1 || !(isAllowedFile(fileExt, 0) || isAllowedFile(fileExt, 1) || isAllowedFile(fileExt, 2)
					|| isAllowedFile(fileExt, 3))) {
				WsError wsError = new WsError();
				wsError.setLevel(ErrorLevel.HIGH);
				wsError.setMessage("Unsupported file.");
				return wsError;
			}
			wsMessage.setMediaType(AppUtil.getMediaType(mainFileName));
			if (MediaType.IMAGE == wsMessage.getMediaType() && !AppConstants.IMAGE_EXT_GIF.equals(fileExt)) {
				fileExt = AppConstants.IMAGE_EXT_JPG;
				mainFileName = mainFileName.substring(0, dotIdx) + fileExt;
				wsMessage.setMainFilename(mainFileName);
			}
			String filename = System.currentTimeMillis() + fileExt;
			/*
			 * int idx = 0; byte[] fileBytes = new byte[byteBuffer.limit() - bodyLen - 1];
			 * for (int i = bodyLen + 1; i < payloadBytes.length; i++) fileBytes[idx++] =
			 * payloadBytes[i];
			 */
			String filePath = createFilePath(wsMessage) + File.separator + filename;
			Files.write(Paths.get(filePath), fileBytes);
			// Set actual file path for storing into database
			wsMessage.setModFilename(filePath);
			saveMessage(wsMessage);
			// Remove the actual path and set relative path for download/render
			wsMessage.setModFilename(AppUtil.getRelativePath(filePath));
			return wsMessage;
		} catch (Exception e) {
			log.error("Failed to process the file.", e);
			WsError wsError = new WsError();
			wsError.setLevel(ErrorLevel.LOW);
			wsError.setMessage("Failed to process the file.");
			return wsError;
		}
	}

	private static String createFilePath(WsMessage wsMessage) {
		StringBuffer uploadPathFinal = new StringBuffer();
		uploadPathFinal.append(AppConstants.UPLOAD_PATH);
		uploadPathFinal.append(AppConstants.ATTACHMENT_FOLDER);
		uploadPathFinal.append(wsMessage.getSender());
		uploadPathFinal.append(File.separator);
		uploadPathFinal.append(wsMessage.getReceivers()[0]);
		uploadPathFinal.append(File.separator);
		uploadPathFinal.append(wsMessage.getMediaType().toString().toLowerCase());
		File f = new File(uploadPathFinal.toString());
		if (!f.exists()) {
			f.mkdirs();
		}

		return f.getAbsolutePath();
	}

	@Transactional(rollbackFor = Exception.class)
	private void saveMessage(WsMessage wsMessage) {
		Message message = new Message();
		message.setSender(new User(wsMessage.getSender()));
		message.setReceiver(new User(wsMessage.getReceivers()[0]));
		message.setContent(AppUtil.decodeText(wsMessage.getContent()));
		message.setAttachmentPath(wsMessage.getModFilename());
		message.setOriginalFilename(wsMessage.getMainFilename());
		message.setSentAt(ZonedDateTime.now());
		message.setReceiverType(wsMessage.getReceiverType());

		AnswerType answerType = wsMessage.getAnswerType();

		boolean isUpdate = false;
		if (answerType != null) {
			message.setAnswerType(answerType);
			if (AnswerType.REPLY == answerType) {
				Message repliedMessage = messageDao.getMessagesById(wsMessage.getReplyOf().getMessageId());
				repliedMessage.setMessageId(wsMessage.getReplyOf().getMessageId());
				message.setReplyOf(repliedMessage);
				isUpdate = true;
			}
		}
		if (isUpdate)
			messageDao.updateMessage(message);
		else
			messageDao.insertMessage(message);

		if (message.getReplyOf() != null) {
			Message repliedMessage = message.getReplyOf();
			WsMessage wsRepliedMessage = wsMessage.getReplyOf();
			wsRepliedMessage.setSender(repliedMessage.getSender().getUsername());
			wsRepliedMessage.setReceivers(new String[] { repliedMessage.getReceiver().getUsername() });
			wsRepliedMessage.setContent(AppUtil.encodeText(repliedMessage.getContent()));
			if (StringUtils.isNotBlank(repliedMessage.getAttachmentPath())) {
				wsRepliedMessage.setMediaType(AppUtil.getMediaType(repliedMessage.getAttachmentPath()));
				wsRepliedMessage.setMainFilename(repliedMessage.getOriginalFilename());
				wsRepliedMessage.setModFilename(AppUtil.getRelativePath(repliedMessage.getAttachmentPath()));
				if (wsRepliedMessage.getContent() != null)
					wsRepliedMessage.setContentType(ContentType.MIXED);
				else
					wsRepliedMessage.setContentType(ContentType.BINARY);
			} else {
				wsRepliedMessage.setContentType(ContentType.TEXT);
			}
		}

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
		String[] target;
		switch (type) {
		case 0:
			target = AppConstants.IMAGE_EXTS;
			break;
		case 1:
			target = AppConstants.AUDIO_EXTS;
			break;
		case 2:
			target = AppConstants.VIDEO_EXTS;
			break;
		case 3:
			target = Arrays.copyOf(AppConstants.DOCU_EXTS,
					AppConstants.DOCU_EXTS.length + AppConstants.PDF_EXTS.length + AppConstants.TEXT_EXTS.length);
			System.arraycopy(AppConstants.PDF_EXTS, 0, target, AppConstants.DOCU_EXTS.length,
					AppConstants.PDF_EXTS.length);
			System.arraycopy(AppConstants.TEXT_EXTS, 0, target,
					AppConstants.DOCU_EXTS.length + AppConstants.PDF_EXTS.length, AppConstants.TEXT_EXTS.length);
			break;
		default:
			target = null;
		}
		if (target == null)
			return false;
		for (String aExt : target) {
			if (aExt.equalsIgnoreCase(ext))
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

	private void changeTimeZone(WebSocketSession session) {
		// Set time zone for this request from WsCommon
		Object obTz = session.getAttributes().get(AppConstants.SESSION_ATTR_USER_TZ);
		if (obTz != null) {
			TimeZone tz = TimeZone.getTimeZone(ZoneId.of(obTz.toString()));
			LocaleContextHolder.setTimeZone(tz);
		}
	}
}
