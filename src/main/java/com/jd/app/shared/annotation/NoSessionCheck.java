package com.jd.app.shared.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author Joydeep Dey
 */
@Documented
@Retention(RUNTIME)
@Target({ TYPE, METHOD })
/**
 * @author Joydeep Dey
 */
public @interface NoSessionCheck {

}
