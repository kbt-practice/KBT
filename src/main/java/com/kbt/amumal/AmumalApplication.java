package com.kbt.amumal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.TimeZone;

@SpringBootApplication
@EnableJpaAuditing
public class AmumalApplication {
	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
		SpringApplication.run(AmumalApplication.class, args);
	}
}

// 테스트를 위한 주석 추가 22