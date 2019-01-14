package com.jd.app;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring boot main class
 * 
 * @author Joydeep Dey
 */
@SpringBootApplication
public class Application {

	/**
	 * Execute spring boot application and and launch it in browser
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		try {
			Runtime.getRuntime().exec("explorer http://www.intracom.tk");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
