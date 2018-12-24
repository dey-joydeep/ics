package com.jd.app.shared.constant.enums;

/**
 * Notify user about the problem with low attention or full attention.
 * {@code LOW} will invoke an auto dismissable pop-up, where as {@code HIGH}
 * will be fixed pop-up which the user will dismiss.
 * 
 * @author Joydeep Dey
 */
public enum ErrorLevel {

	/** Level: Low **/
	LOW,

	/** Level: High **/
	HIGH
}
