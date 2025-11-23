package com.sporty.eventtracker.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScorePollingServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ScoreUpdateProducer producer;

    private ScorePollingService scorePollingService;

    @BeforeEach
    void setUp() {
        // Manually inject mocks and a dummy URL
        scorePollingService = new ScorePollingService(restTemplate, producer, "http://mock-api/");
    }

    @Test
    void pollSingleEvent_ShouldPublishScore_WhenApiReturnsData() {
        // Arrange
        String eventId = "match-123";
        Map<String, String> mockResponse = Map.of("eventId", eventId, "currentScore", "1:0");

        when(restTemplate.getForObject(eq("http://mock-api/" + eventId), eq(Map.class)))
                .thenReturn(mockResponse);

        // Act
        scorePollingService.pollSingleEvent(eventId);

        // Assert
        verify(producer).sendScoreUpdate(mockResponse); // Verify Kafka producer was called
    }

    @Test
    void pollSingleEvent_ShouldHandleApiError_Gracefully() {
        // Arrange
        String eventId = "match-error";
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenThrow(new RuntimeException("API Down"));

        // Act
        scorePollingService.pollSingleEvent(eventId);

        // Assert
        verify(producer, never()).sendScoreUpdate(any()); // Ensure we didn't publish garbage
        // Verify logs (implicit check: method finished without throwing exception)
    }
}