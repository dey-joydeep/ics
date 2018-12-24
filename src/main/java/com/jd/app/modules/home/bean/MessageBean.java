package com.jd.app.modules.home.bean;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jd.app.modules.SharedBean;
import com.jd.app.shared.constant.enums.AcknowledgeType;
import com.jd.app.shared.helper.json.deserializer.JsonDateTimeDeserializer;
import com.jd.app.shared.helper.json.serializer.JsonDateTimeSerializer;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Joydeep Dey
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class MessageBean extends SharedBean {

	private long messageId;
	private String sender;
	private String receiver;
	private String content;
	private String attachmentPathMedia;
	private String attachmentPathDoc;
	private AcknowledgeType messageStatus;
	@JsonSerialize(using = JsonDateTimeSerializer.class)
	@JsonDeserialize(using = JsonDateTimeDeserializer.class)
	private ZonedDateTime sentAt;
	@JsonSerialize(using = JsonDateTimeSerializer.class)
	@JsonDeserialize(using = JsonDateTimeDeserializer.class)
	private ZonedDateTime deliveredAt;
	@JsonSerialize(using = JsonDateTimeSerializer.class)
	@JsonDeserialize(using = JsonDateTimeDeserializer.class)
	private ZonedDateTime readAt;
}
