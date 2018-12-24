package com.jd.app;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Joydeep Dey
 */
@SpringBootApplication
public class Application {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		try {
			Runtime.getRuntime().exec("explorer http://localhost/");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
