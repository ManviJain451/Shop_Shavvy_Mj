package com.shopsavvy.shopshavvy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ShopShavvyApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopShavvyApplication.class, args);
	}

}
