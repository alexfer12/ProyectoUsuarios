package com.example.mediumRoles;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync // Habilitar soporte para Async
@EnableCaching
@EnableScheduling
public class MediumRolesApplication {

	public static void main(String[] args) {
		SpringApplication.run(MediumRolesApplication.class, args);
	}

}
