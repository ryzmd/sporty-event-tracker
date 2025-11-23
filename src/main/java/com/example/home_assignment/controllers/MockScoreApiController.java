package com.example.home_assignment.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
public class MockScoreApiController {

    private static final Logger logger = LoggerFactory.getLogger(MockScoreApiController.class);
    private final Random random = new Random(); 

    @GetMapping("/mock-api/score/{eventId}")
    public Map<String, String> getMockScore(@PathVariable("eventId") String eventId) {
        try {
            if (eventId == null) {
                throw new IllegalArgumentException("Event ID cannot be null");
            }

            // Generate random scores
            int homeScore = random.nextInt(4); // 0-3
            int awayScore = random.nextInt(4); // 0-3
            String currentScore = homeScore + ":" + awayScore;

            Map<String, String> response = new HashMap<>();
            response.put("eventId", eventId);
            response.put("currentScore", currentScore);

            logger.debug("Generated mock score for {}: {}", eventId, currentScore);
            return response;

        } catch (Exception e) {
            logger.error("CRASH inside MockController for event {}: {}", eventId, e.getMessage(), e);
            throw e;
        }
    }
}