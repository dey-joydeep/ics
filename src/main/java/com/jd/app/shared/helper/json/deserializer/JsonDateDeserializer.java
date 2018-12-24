package com.jd.app.shared.helper.json.deserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.jd.app.shared.constant.general.AppConstants;

/**
 * @author Joydeep Dey
 */
public class JsonDateDeserializer extends JsonDeserializer<LocalDate> {
	public LocalDate deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException, JsonProcessingException {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(AppConstants.YYYYMMDD);
		return LocalDate.parse(jsonParser.getText(), formatter);
	}

}
