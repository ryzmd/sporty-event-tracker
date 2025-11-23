package com.sporty.eventtracker.dto;

public record EventStatusUpdate(
    String eventId,
    EventStatus status
) {
}

