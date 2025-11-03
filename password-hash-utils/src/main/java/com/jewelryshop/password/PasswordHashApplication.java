package com.jewelryshop.password;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Main application class for the Password Hash Generator
 * This is a standalone Spring Boot application
 */
@SpringBootApplication
public class PasswordHashApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(PasswordHashApplication.class, args);
    }
    
    /**
     * Bean for BCryptPasswordEncoder
     * Can be configured with different strength levels
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}





