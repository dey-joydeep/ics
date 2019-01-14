package com.jd.app.websocket.bean;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jd.app.shared.constant.enums.AnswerType;
import com.jd.app.shared.constant.enums.CommType;
import com.jd.app.shared.constant.enums.ContentType;
import com.jd.app.shared.constant.enums.MediaType;
import com.jd.app.shared.constant.enums.ReceiverType;
import com.jd.app.shared.helper.json.deserializer.JsonDateTimeDeserializer;
import com.jd.app.shared.helper.json.serializer.JsonDateTimeSerializer;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Joydeep Dey
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class WsMessage extends WsCommon {

	public WsMessage() {
		super.setCommType(CommType.MSG);
	}

	private long messageId;
	private String mainFilename;
	private String modFilename;
	private String content;
	private ReceiverType receiverType;
	@JsonSerialize(using = JsonDateTimeSerializer.class)
	@JsonDeserialize(using = JsonDateTimeDeserializer.class)
	private ZonedDateTime sentAt;
	private ContentType contentType;
	private MediaType mediaType;
	private AnswerType answerType;
	private WsMessage replyOf;
}
