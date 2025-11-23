package com.sporty.eventtracker.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class ScorePollingService {

    private static final Logger logger = LoggerFactory.getLogger(ScorePollingService.class);
    private final RestTemplate restTemplate;
    private final ScoreUpdateProducer producer;
    private final String externalApiUrl;

    public ScorePollingService(RestTemplate restTemplate, 
                               ScoreUpdateProducer producer,
                               @Value("${app.external-api.url:http://localhost:8080/mock-api/score/}") String externalApiUrl) {
        this.restTemplate = restTemplate;
        this.producer = producer;
        this.externalApiUrl = externalApiUrl;
    }

    /**
     * This method contains the core business logic for a single poll.
     */
    public void pollSingleEvent(String eventId) {
        String fullUrl = externalApiUrl + eventId;
        logger.debug("Starting poll for event: {}", eventId);

        try {
            // Call External API
            // Structure: { "eventId": "1234", "currentScore": "0:0" }
            Map<String, String> apiResponse = restTemplate.getForObject(fullUrl, Map.class);

            if (apiResponse == null || apiResponse.isEmpty()) {
                logger.warn("Received empty response for event: {}", eventId);
                return;
            }

            // Publish to Kafka
            producer.sendScoreUpdate(apiResponse); 
            logger.info("Successfully polled and published score for event: {}", eventId);

        } catch (Exception e) {
            // We catch Exception to ensure one bad event doesn't crash the scheduler thread.
            // In a real app, we can catch specific exceptions (RestClientException, KafkaException)
            logger.error("Error polling score for event {}: {}", eventId, e.getMessage(), e);
        }
    }
}