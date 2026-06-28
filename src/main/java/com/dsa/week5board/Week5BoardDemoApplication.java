package com.dsa.week5board;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Week5BoardDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(Week5BoardDemoApplication.class, args);
	}
}
