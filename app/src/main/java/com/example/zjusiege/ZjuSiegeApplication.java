package com.example.zjusiege;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ZjuSiegeApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZjuSiegeApplication.class, args);
	}
}
