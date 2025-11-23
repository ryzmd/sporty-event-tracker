package com.example.home_assignment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HomeAssignmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(HomeAssignmentApplication.class, args);
    }
}