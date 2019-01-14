package com.jd.app.db.entity;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.jd.app.db.entity.common.CommonEntity;
import com.jd.app.shared.constant.enums.AnswerType;
import com.jd.app.shared.constant.enums.ReceiverType;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "message")
@EqualsAndHashCode(callSuper = false, exclude = { "sender", "receiver" })
public class Message extends CommonEntity {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "message_id", insertable = false, updatable = false)
	private long messageId;

	@ManyToOne
	@JoinColumn(name = "sender")
	private User sender;

	@ManyToOne
	@JoinColumn(name = "receiver")
	private User receiver;

	@Column(name = "content")
	private String content;

	@Column(name = "attachment_path")
	private String attachmentPath;

	@Column(name = "original_filename")
	private String originalFilename;

	@Column(name = "sent_at", columnDefinition = "datetime")
	private ZonedDateTime sentAt;

	@Column(name = "delivered_at", columnDefinition = "datetime")
	private ZonedDateTime deliveredAt;

	@Column(name = "read_at", columnDefinition = "datetime")
	private ZonedDateTime readAt;

	@Enumerated
	@Column(name = "receiver_type", columnDefinition = "tinyint")
	private ReceiverType receiverType;

	@Enumerated
	@Column(name = "answer_type", columnDefinition = "tinyint")
	private AnswerType answerType;

	@Column(name = "is_sender_delete")
	private boolean senderDelete;

	@Column(name = "is_receiver_delete")
	private boolean receiverDelete;
	
	@OneToOne
	@JoinColumn(name = "reply_of_id")
	private Message replyOf;
}
