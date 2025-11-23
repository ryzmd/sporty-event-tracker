package com.example.home_assignment.services.schedulers;

import com.example.home_assignment.services.ScorePollingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class GlobalEventSchedulerTest {

    @Mock
    private ScorePollingService pollingService;

    @InjectMocks
    private GlobalEventScheduler globalScheduler;

    @Test
    void pollAllEvents_ShouldPollAllActiveEvents() {
        // Arrange
        String event1 = "match-A";
        String event2 = "match-B";

        // Add two events to the "Live" set
        globalScheduler.startTracking(event1);
        globalScheduler.startTracking(event2);

        // Act
        // Simulate the @Scheduled timer firing
        globalScheduler.pollAllEvents();

        // Assert
        // Verify the service was called once for EACH event
        verify(pollingService, times(1)).pollSingleEvent(event1);
        verify(pollingService, times(1)).pollSingleEvent(event2);
    }

    @Test
    void stopTracking_ShouldRemoveEventFromPollingLoop() {
        // Arrange
        String event1 = "match-A";
        globalScheduler.startTracking(event1);

        // Run once to prove it's there
        globalScheduler.pollAllEvents();
        verify(pollingService, times(1)).pollSingleEvent(event1);

        // Act
        globalScheduler.stopTracking(event1); // Remove it
        globalScheduler.pollAllEvents(); // Run loop again

        // Assert
        // Should still be '1' from the first run, NOT '2'
        verify(pollingService, times(1)).pollSingleEvent(event1);
    }

    @Test
    void pollAllEvents_ShouldDoNothing_WhenNoEventsAreLive() {
        // Act
        globalScheduler.pollAllEvents();

        // Assert
        verifyNoInteractions(pollingService);
    }
}