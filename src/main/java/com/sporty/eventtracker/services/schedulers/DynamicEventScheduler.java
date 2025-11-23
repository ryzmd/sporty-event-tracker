package com.sporty.eventtracker.services.schedulers;

import com.sporty.eventtracker.interfaces.EventScheduler;
import com.sporty.eventtracker.services.ScorePollingService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@ConditionalOnProperty(name = "app.scheduling.mode", havingValue = "dynamic", matchIfMissing = true)
public class DynamicEventScheduler implements EventScheduler {

    private static final Duration POLLING_INTERVAL = Duration.ofSeconds(10);
    private final TaskScheduler taskScheduler;
    private final ScorePollingService pollingService; 
    private static final Logger logger = LoggerFactory.getLogger(ScorePollingService.class);

    private final Map<String, ScheduledFuture<?>> activeTasks = new ConcurrentHashMap<>();

    public DynamicEventScheduler(TaskScheduler taskScheduler, ScorePollingService pollingService) {
        this.taskScheduler = taskScheduler;
        this.pollingService = pollingService;
    }

    @Override
    public void startTracking(String eventId) {
        activeTasks.computeIfAbsent(eventId, id -> {
            logger.info("Starting dynamic tracking for event: {}", id);

            return taskScheduler.scheduleAtFixedRate(() -> {
                try {
                    org.slf4j.MDC.put("eventId", id);

                    pollingService.pollSingleEvent(id);
                } finally {
                    org.slf4j.MDC.remove("eventId");
                }
            }, POLLING_INTERVAL);
        });
    }

    @Override
    public void stopTracking(String eventId) {
        ScheduledFuture<?> future = activeTasks.remove(eventId);
        if (future != null) {
            future.cancel(false); // Cancel the specific task
        }
    }
}