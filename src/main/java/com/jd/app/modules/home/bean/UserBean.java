package com.jd.app.modules.home.bean;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jd.app.modules.SharedBean;
import com.jd.app.shared.constant.enums.Gender;
import com.jd.app.shared.helper.json.deserializer.JsonDateTimeDeserializer;
import com.jd.app.shared.helper.json.serializer.JsonDateTimeSerializer;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Joydeep Dey
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class UserBean extends SharedBean {

	private String firstname;
	private String lastname;
	private Gender gender;
	@JsonSerialize(using = JsonDateTimeSerializer.class)
	@JsonDeserialize(using = JsonDateTimeDeserializer.class)
	private ZonedDateTime lastLoginDateTime;
	private String avatar;
}
