package com.jd.app.modules.home.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jd.app.db.dao.def.MessageDao;
import com.jd.app.db.dao.def.UserDao;
import com.jd.app.db.entity.Message;
import com.jd.app.db.entity.User;
import com.jd.app.modules.home.bean.MemberBean;
import com.jd.app.modules.home.bean.MessageBean;
import com.jd.app.modules.home.bean.UserBean;
import com.jd.app.shared.constant.enums.AcknowledgeType;
import com.jd.app.shared.constant.enums.Gender;
import com.jd.app.shared.helper.AppUtil;
import com.jd.app.websocket.bean.GlobalResource;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class HomeServiceImpl implements HomeService {

	@Autowired
	private UserDao userDao;

	@Autowired
	private MessageDao messageDao;

	@Transactional(readOnly = true)
	public UserBean getSelfDetails(String username) {
		UserBean userBean = null;
		try {
			User user = userDao.getUserByUsername(username);
			if (user != null) {
				userBean = new UserBean();
				userBean.setUsername(user.getUsername());
				userBean.setFirstname(user.getFirstname());
				userBean.setLastname(user.getLastname());
				userBean.setLastLoginDateTime(user.getLogin().getLastLoginAt());
				if (user.getAvatarPath() != null) {
					userBean.setAvatar(user.getAvatarPath());
				} else {
					if (user.getGender() == Gender.MALE)
						userBean.setAvatar("./images/png/av_gen_ml.png");
					else
						userBean.setAvatar("./images/png/av_gen_fml.png");
				}
			}
		} catch (Exception e) {
			log.error(e);
		}
		return userBean;
	}

	@Transactional(readOnly = true)
	public List<MemberBean> getMembers(String username) {
		List<MemberBean> memberList = null;
		try {
			memberList = new ArrayList<>();
			List<User> userList = userDao.getActiveUsers(username);
			for (User user : userList) {
				MemberBean member = new MemberBean();
				member.setMemberId(user.getUsername());
				member.setMemberName(user.getFirstname() + " " + user.getLastname());
				if (user.getAvatarPath() != null) {
					member.setAvatar(user.getAvatarPath());
				} else {
					if (user.getGender() == Gender.MALE)
						member.setAvatar("./images/png/av_gen_ml.png");
					else
						member.setAvatar("./images/png/av_gen_fml.png");
				}
				if (GlobalResource.ACTIVE_SESSION_HOLDER.containsKey(user.getUsername()))
					member.setOnline(true);
				else
					member.setLastOnlineAt(user.getLogin().getLastOnlineAt());
				Message message = messageDao.getLastMessage(user.getUsername(), username);
				if (message != null) {
					MessageBean messageBean = new MessageBean();
					messageBean.setMessageId(message.getMessageId());
					messageBean.setSender(message.getSender().getUsername());
					messageBean.setReceiver(message.getReceiver().getUsername());
					messageBean.setContent(message.getContent());
					if (message.getReadAt() != null)
						messageBean.setMessageStatus(AcknowledgeType.READ);
					else if (message.getDeliveredAt() != null)
						messageBean.setMessageStatus(AcknowledgeType.DELIVERED);
					else
						messageBean.setMessageStatus(AcknowledgeType.SENT);
					messageBean.setSentAt(message.getSentAt());
					messageBean.setSuccess(true);
					member.setLastMessage(messageBean);

					long unreadCount = messageDao.getUnreadMessagesCount(member.getMemberId(), username);
					member.setUnreadMessageCount((int) unreadCount);
				}
				member.setSuccess(true);
				memberList.add(member);
			}
		} catch (Exception e) {
			log.error(e);
		}
		return memberList;
	}

	@Override
	@Transactional(readOnly = true)
	public List<MessageBean> getMessages(String sender, String receiver) {
		List<MessageBean> messages = new ArrayList<>();
		try {
			List<Message> messageList = messageDao.getMessages(sender, receiver);
			for (Message message : messageList) {
				MessageBean messageBean = new MessageBean();
				messageBean.setMessageId(message.getMessageId());
				messageBean.setSender(message.getSender().getUsername());
				messageBean.setReceiver(message.getReceiver().getUsername());
				messageBean.setContent(AppUtil.encodeText(message.getContent()));
				if (message.getReadAt() != null) {
					messageBean.setReadAt(message.getReadAt());
					messageBean.setDeliveredAt(message.getDeliveredAt());
					messageBean.setMessageStatus(AcknowledgeType.READ);
				} else if (message.getDeliveredAt() != null) {
					messageBean.setDeliveredAt(message.getDeliveredAt());
					messageBean.setMessageStatus(AcknowledgeType.DELIVERED);
				} else {
					messageBean.setMessageStatus(AcknowledgeType.SENT);
				}
				messageBean.setSentAt(message.getSentAt());
				messageBean.setSuccess(true);
				messages.add(messageBean);
			}
		} catch (Exception e) {
			log.error(e);
		}
		return messages;
	}
}
