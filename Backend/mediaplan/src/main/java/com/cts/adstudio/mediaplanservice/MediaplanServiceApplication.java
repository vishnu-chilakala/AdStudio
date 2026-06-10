package com.cts.adstudio.mediaplanservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MediaplanServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(MediaplanServiceApplication.class, args);
	}

}
