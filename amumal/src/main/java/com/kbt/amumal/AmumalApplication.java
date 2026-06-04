package com.kbt.amumal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AmumalApplication {
	public static void main(String[] args) {
		SpringApplication.run(AmumalApplication.class, args);
	}
}
