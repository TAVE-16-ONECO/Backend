package com.oneco.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.oneco.backend")
public class OnecoApplication {

	public static void main(String[] args) {
		SpringApplication.run(OnecoApplication.class, args);
	}


}
