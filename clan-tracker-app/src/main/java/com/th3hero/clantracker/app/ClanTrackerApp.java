package com.th3hero.clantracker.app;

import com.kseth.development.autoconfigure.jpa.DataJpaAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan(basePackages = "com.th3hero.clantracker.*")
@ComponentScan(basePackages = "com.th3hero.clantracker.*")
@EnableJpaRepositories(basePackages = "com.th3hero.clantracker.*")
@SpringBootApplication(exclude = {DataJpaAutoConfiguration.class})
public class ClanTrackerApp {

    public static void main(String[] args) {
        SpringApplication.run(ClanTrackerApp.class, args);
    }

}
