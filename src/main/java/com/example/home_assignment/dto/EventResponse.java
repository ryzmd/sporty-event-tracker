package com.example.home_assignment.dto;

import java.time.LocalDateTime;

public record EventResponse(
    String eventId,
    String status,
    String message,
    LocalDateTime timestamp
) {
    public static EventResponse success(String eventId, String status, String message) {
        return new EventResponse(eventId, status, message, LocalDateTime.now());
    }
    
    public static EventResponse error(String eventId, String message) {
        return new EventResponse(eventId, "ERROR", message, LocalDateTime.now());
    }
}