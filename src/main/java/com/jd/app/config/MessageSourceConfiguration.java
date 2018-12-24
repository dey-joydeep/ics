package com.jd.app.config;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.Stream;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import com.jd.app.shared.constant.general.CookieNames;

/**
 * @author Joydeep Dey
 */
@Configuration
public class MessageSourceConfiguration {

	private final Comparator<String> byLength = (e1, e2) -> e1 == null || e2 == null ? -1
			: e1.length() > e2.length() ? -1 : 1;
	private static final List<String> FILE_PATH_LIST = new ArrayList<>();
	private static final String PROPERTIES_BASE_PATH = "C:/application/external_resources/properties/";

	static {
		try (Stream<Path> paths = Files.walk(Paths.get(new File(PROPERTIES_BASE_PATH).toURI()))) {
			paths.filter(p -> Files.isRegularFile(p) && !p.endsWith(".properties")).forEach(f -> {
				String path = f.toAbsolutePath().toString();
				path = path.replace(".properties", "");
				FILE_PATH_LIST.add(path);
			});
		} catch (Exception e) {
			throw new Error("Unable to load properties files from the path: " + PROPERTIES_BASE_PATH, e);
		}
	}

	/**
	 * User locale resolver
	 * 
	 * @return cookie locale resolver
	 */
	@Bean
	public LocaleResolver localeResolver() {
		CookieLocaleResolver localeResolver = new CookieLocaleResolver();
		localeResolver.setLanguageTagCompliant(true);
		localeResolver.setDefaultLocale(Locale.US);
		localeResolver.setDefaultTimeZone(TimeZone.getTimeZone("UTC"));
		localeResolver.setCookieName(CookieNames.LANGUAGE);
		return localeResolver;
	}

	/**
	 * @return message source
	 */
	@Bean
	@Primary
	public MessageSource messageSource() {

		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		String[] baseNames = new String[2];
		baseNames[0] = getBaseUriSring("info");
		baseNames[1] = getBaseUriSring("error");
		messageSource.setBasenames(baseNames);
		messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
		messageSource.setCacheSeconds(3600); // Cache for 1 hour and then re-check
		return messageSource;
	}

	/**
	 * @return message source
	 */
	@Bean(name = "appUi")
	public MessageSource appUiDetails() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		List<String> filteredList = new ArrayList<>();
		String subDirPath = "client" + File.separator;
		FILE_PATH_LIST.stream().filter(f -> f.contains(subDirPath)).forEach(f -> {
			String folder = f.substring(0, f.lastIndexOf(File.separator) + 1);
			String file = new File(folder + folder.substring(folder.lastIndexOf(File.separator) + 1)).toURI()
					.toString();
			if (!filteredList.contains(file))
//				filteredList.add(getBaseUriSring(subDirPath + ));
				filteredList.add(file);
		});

		messageSource.setBasenames(filteredList.stream().toArray(String[]::new));
		messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
		messageSource.setCacheSeconds(3600); // Cache for 1 hour and then re-check
		return messageSource;
	}

	/**
	 * @return message source
	 */
	@Bean(name = "dateTimeFormats")
	public MessageSource dateTimeFormats() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasenames(getBaseUriSring("datetime"));
		messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
		messageSource.setCacheSeconds(3600); // Cache for 1 hour and then re-check
		return messageSource;
	}

	private String getBaseUriSring(String resourceName) {
		String base = FILE_PATH_LIST.stream()
				.filter(p -> p.startsWith(new File(PROPERTIES_BASE_PATH + resourceName).getAbsolutePath()))
				.sorted(byLength.reversed()).findFirst().get();
		return new File(base).toURI().toString();
	}
}
