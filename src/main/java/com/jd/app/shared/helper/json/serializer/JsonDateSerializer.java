package com.jd.app.shared.helper.json.serializer;

import java.io.IOException;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * @author Joydeep Dey
 */
public class JsonDateSerializer extends ParentDateTimeSerializer {

	public void serialize(ZonedDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		setFormatKey("date");
		super.serialize(value, gen, serializers);
	}
}
