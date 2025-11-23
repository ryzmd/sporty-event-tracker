package com.sporty.eventtracker.interfaces;

public interface EventScheduler {
    void startTracking(String eventId);
    void stopTracking(String eventId);
}

