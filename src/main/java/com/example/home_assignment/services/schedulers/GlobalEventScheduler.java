package com.example.home_assignment.services.schedulers;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import com.example.home_assignment.interfaces.EventScheduler;
import com.example.home_assignment.services.ScorePollingService;

import org.slf4j.MDC; 

@Service
@ConditionalOnProperty(name = "app.scheduling.mode", havingValue = "global")
public class GlobalEventScheduler implements EventScheduler {

    private final Set<String> liveEvents = ConcurrentHashMap.newKeySet();
    private final ScorePollingService pollingService;

    public GlobalEventScheduler(ScorePollingService pollingService) {
        this.pollingService = pollingService;
    }

    @Override
    public void startTracking(String eventId) {
        liveEvents.add(eventId);
    }

    @Override
    public void stopTracking(String eventId) {
        liveEvents.remove(eventId);
    }

    // This runs single job for all events
    @Scheduled(fixedRateString = "${app.scheduling.fixed-rate}") 
    public void pollAllEvents() {
        liveEvents.forEach(eventId -> {
            try {
                MDC.put("eventId", eventId);
                
                pollingService.pollSingleEvent(eventId); 
            } finally {
                MDC.remove("eventId");
            }
        });
    }
}