package com.jd.app.shared.helper.json.deserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * @author Joydeep Dey
 */
public class JsonTsDeserializer extends JsonDeserializer<LocalDateTime> {
	public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException, JsonProcessingException {
		long ts = Long.parseLong(jsonParser.getText());
		return Instant.ofEpochMilli(ts).atZone(ZoneId.systemDefault()).toLocalDateTime();
	}

}
