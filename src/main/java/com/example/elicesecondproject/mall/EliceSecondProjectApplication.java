package com.example.elicesecondproject.mall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class EliceSecondProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(EliceSecondProjectApplication.class, args);
    }

}
