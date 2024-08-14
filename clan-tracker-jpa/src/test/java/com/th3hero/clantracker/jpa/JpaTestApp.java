package com.th3hero.clantracker.jpa;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.th3hero.clantracker.*")
@ComponentScan(basePackages = "com.th3hero.clantracker.*")
@EnableJpaRepositories(basePackages = "com.th3hero.clantracker.*")
public class JpaTestApp {

}
