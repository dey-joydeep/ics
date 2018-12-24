package com.jd.app.websocket.bean;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jd.app.shared.constant.enums.CommType;
import com.jd.app.shared.helper.json.deserializer.JsonDateTimeDeserializer;
import com.jd.app.shared.helper.json.serializer.JsonDateTimeSerializer;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Joydeep Dey
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class WsAcknowledge extends WsCommon {

	public WsAcknowledge() {
		super.setCommType(CommType.ACK);
	}

	private long[] messageIds;
	private boolean delivered;
	@JsonSerialize(using = JsonDateTimeSerializer.class)
	@JsonDeserialize(using = JsonDateTimeDeserializer.class)
	private ZonedDateTime deliveredAt;
	private boolean read;
	@JsonSerialize(using = JsonDateTimeSerializer.class)
	@JsonDeserialize(using = JsonDateTimeDeserializer.class)
	private ZonedDateTime readAt;
}
