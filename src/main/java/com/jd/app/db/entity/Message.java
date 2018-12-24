package com.jd.app.db.entity;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.jd.app.db.entity.common.CommonEntity;
import com.jd.app.shared.constant.enums.AnswerType;
import com.jd.app.shared.constant.enums.ReceiverType;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "message")
@EqualsAndHashCode(callSuper = false)
public class Message extends CommonEntity {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "message_id", insertable = false, updatable = false)
	private long messageId;

	@JoinColumn(name = "sender")
	@ManyToOne(fetch = FetchType.LAZY)
	private User sender;

	@JoinColumn(name = "receiver")
	@ManyToOne(fetch = FetchType.LAZY)
	private User receiver;

	@Column(name = "content")
	private String content;

	@Column(name = "attachment_path_media")
	private String attachmentPathMedia;

	@Column(name = "attachment_path_doc")
	private String attachmentPathDoc;

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
}
