package com.jd.app.shared.helper.json.serializer;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * @author Joydeep Dey
 */
public class ParentDateTimeSerializer extends JsonSerializer<ZonedDateTime> {

	@Autowired
	@Qualifier("dateTimeFormats")
	private MessageSource dateTimeFormats;

	private String formatKey;

	/**
	 * @param formatKey the formatKey to set
	 */
	public void setFormatKey(String formatKey) {
		this.formatKey = formatKey;
	}

	public void serialize(ZonedDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

		TimeZone tzc = LocaleContextHolder.getTimeZone();
		ZonedDateTime dateTime = value.withZoneSameInstant(tzc.toZoneId());
		DateTimeFormatter formatter = DateTimeFormatter
				.ofPattern(dateTimeFormats.getMessage(formatKey, null, LocaleContextHolder.getLocale()))
				.localizedBy(LocaleContextHolder.getLocale());
		gen.writeString(dateTime.format(formatter));

	}
}
