package com.gomitas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GomipetsApplication {

	public static void main(String[] args) {
		SpringApplication.run(GomipetsApplication.class, args);
	}

}
