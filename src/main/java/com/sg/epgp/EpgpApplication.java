package com.sg.epgp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.sg.epgp.*")
public class EpgpApplication {

	public static void main(String[] args) {
		SpringApplication.run(EpgpApplication.class, args);
	}

}
