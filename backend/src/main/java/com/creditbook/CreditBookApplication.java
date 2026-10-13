package com.creditbook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class CreditBookApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreditBookApplication.class, args);
    }
}
