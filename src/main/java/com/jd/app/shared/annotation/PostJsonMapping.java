package com.jd.app.shared.annotation;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Joydeep Dey
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@RequestMapping(consumes = APPLICATION_JSON_UTF8_VALUE, produces = APPLICATION_JSON_UTF8_VALUE)
public @interface PostJsonMapping {
	@AliasFor(annotation = RequestMapping.class, attribute = "value")
	String[] value() default {};

	@AliasFor(annotation = RequestMapping.class, attribute = "method")
	RequestMethod[] method() default { RequestMethod.POST };

	@AliasFor(annotation = RequestMapping.class, attribute = "params")
	String[] params() default {};

	@AliasFor(annotation = RequestMapping.class, attribute = "headers")
	String[] headers() default {};

	@AliasFor(annotation = RequestMapping.class, attribute = "consumes")
	String[] consumes() default {};

	@AliasFor(annotation = RequestMapping.class, attribute = "produces")
	String[] produces() default {};
}
