package com.example.home_assignment.controllers;

import com.example.home_assignment.dto.EventStatus;
import com.example.home_assignment.dto.EventStatusUpdate;
import com.example.home_assignment.interfaces.EventScheduler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.home_assignment.dto.EventResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

@RestController
@RequestMapping("/events")
public class EventController {
    
    private static final Logger logger = LoggerFactory.getLogger(EventController.class); // SLF4J Logger
    private final EventScheduler eventScheduler; // Spring injects the correct one

    public EventController(EventScheduler eventScheduler) {
        this.eventScheduler = eventScheduler;
    }

    @PostMapping("/status")
    public ResponseEntity<EventResponse> updateStatus(@RequestBody EventStatusUpdate update) {
        
        // 1. Validation
        if (update.eventId() == null || update.eventId().isBlank() || update.status() == null) {
            logger.warn("Received invalid status update: {}", update);
            return ResponseEntity.badRequest()
                    .body(EventResponse.error(update.eventId(), "Invalid eventId or status"));
        }

        // 2. Set MDC
        MDC.put("eventId", update.eventId()); 
        logger.info("Received status update: {}", update.status());

        String message;

        // 3. Execute Logic
        if (update.status() == EventStatus.LIVE) {
            eventScheduler.startTracking(update.eventId());
            message = "Tracking started successfully";
        } else {
            eventScheduler.stopTracking(update.eventId());
            message = "Tracking stopped successfully";
        }
        
        // 4. Return Structured JSON
        return ResponseEntity.ok(
            EventResponse.success(update.eventId(), update.status().name(), message)
        );
    }
}