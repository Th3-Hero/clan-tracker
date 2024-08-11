package com.th3hero.clantracker.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@EnableJpaRepositories
@SpringBootApplication
public class ClanTrackerApp {

	public static void main(String[] args) {
		SpringApplication.run(ClanTrackerApp.class, args);
	}

}
