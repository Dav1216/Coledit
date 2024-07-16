package com.coledit.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication
@EnableAutoConfiguration(exclude = { SecurityAutoConfiguration.class })
public class BackendApplication {

    public static void main(String[] args) {
        System.out.println("here5man");
        SpringApplication.run(BackendApplication.class, args);
    }
}
