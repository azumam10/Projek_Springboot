package com.ukm.ukm_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class UkmAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(UkmAppApplication.class, args);
    }
}