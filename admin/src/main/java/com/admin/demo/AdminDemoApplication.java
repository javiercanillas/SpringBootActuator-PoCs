package com.admin.demo;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableAdminServer
@SpringBootApplication
public class AdminDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(AdminDemoApplication.class, args);
	}

}
