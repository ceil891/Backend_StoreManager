package org.example.storemanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;

@SpringBootApplication(exclude = {RedisRepositoriesAutoConfiguration.class})
public class StoremanagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(StoremanagerApplication.class, args);
	}

}

