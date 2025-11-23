package com.sporty.eventtracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SportyEventTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SportyEventTrackerApplication.class, args);
    }
}