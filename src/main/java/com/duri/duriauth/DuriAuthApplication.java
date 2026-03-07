package com.duri.duriauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@ConfigurationPropertiesScan
@SpringBootApplication
public class DuriAuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(DuriAuthApplication.class, args);
	}

}
