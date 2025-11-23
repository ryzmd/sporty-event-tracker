package com.sporty.eventtracker.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ScoreUpdateProducer {
    private static final Logger logger = LoggerFactory.getLogger(ScoreUpdateProducer.class);

    @Value("${app.kafka.topic}")
    private String topic;

    @Autowired
    private KafkaTemplate<String, Map<String, String>> kafkaTemplate;

    public void sendScoreUpdate(Map<String, String> scoreUpdate) {
        String eventId = scoreUpdate.get("eventId");

        kafkaTemplate.send(topic, eventId, scoreUpdate)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        logger.debug("Message persisted by Broker for event {}", eventId);
                    } else {
                        logger.error("Failed to publish event {}: {}", eventId, ex.getMessage());
                    }
                });
    }
}