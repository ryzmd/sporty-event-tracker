package com.example.home_assignment.services.schedulers;

import com.example.home_assignment.services.ScorePollingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DynamicEventSchedulerTest {

    @Mock
    private TaskScheduler taskScheduler;

    @Mock
    private ScorePollingService pollingService;

    @InjectMocks
    private DynamicEventScheduler scheduler;

    @Test
    void startTracking_ShouldScheduleTask_WhenNotAlreadyTracking() {
        // Arrange
        String eventId = "event-1";
        when(taskScheduler.scheduleAtFixedRate(any(Runnable.class), any(Duration.class)))
                .thenReturn(mock(ScheduledFuture.class));

        // Act
        scheduler.startTracking(eventId);

        // Assert
        verify(taskScheduler, times(1)).scheduleAtFixedRate(any(Runnable.class), eq(Duration.ofSeconds(10)));
    }

    @Test
    void startTracking_ShouldNotScheduleDuplicate_WhenAlreadyTracking() {
        // Arrange
        String eventId = "event-1";
        when(taskScheduler.scheduleAtFixedRate(any(Runnable.class), any(Duration.class)))
                .thenReturn(mock(ScheduledFuture.class));

        // Act
        scheduler.startTracking(eventId);
        scheduler.startTracking(eventId); // Call it a second time

        // Assert
        // Should still only be called ONCE due to computeIfAbsent
        verify(taskScheduler, times(1)).scheduleAtFixedRate(any(Runnable.class), any(Duration.class));
    }
}