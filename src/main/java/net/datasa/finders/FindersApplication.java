package net.datasa.finders;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class FindersApplication {

    public static void main(String[] args) {
        SpringApplication.run(FindersApplication.class, args);
    }

}
