package com.wfm.experts.notificationengine.service.impl;

import com.wfm.experts.notificationengine.dto.NotificationRequest;
import com.wfm.experts.notificationengine.entity.AppNotification;
import com.wfm.experts.notificationengine.repository.AppNotificationRepository;
import com.wfm.experts.notificationengine.service.TemplatingService;
import com.wfm.experts.tenancy.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppNotificationServiceImplTest {

    @Mock
    private AppNotificationRepository appNotificationRepository;

    @Mock
    private TemplatingService templatingService; // Mock if buildAppNotificationEntity uses it heavily

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private AppNotificationServiceImpl appNotificationService;

    private final String TEST_TENANT_ID = "test-tenant";
    private final String BROADCAST_REQUEST_ID = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        // Set the tenant context for the test
        TenantContext.setTenant(TEST_TENANT_ID);

        // Mock repository save to return the entity passed to it
        when(appNotificationRepository.save(any(AppNotification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Mock templating service if needed for buildAppNotificationEntity
        // For simplicity, assuming direct payload usage or simple mock for templating
        when(templatingService.getAndRenderTemplate(anyString(), any(), anyString(), anyMap()))
                .thenReturn(Optional.of(new TemplatingService.RenderedTemplateContent("Test Subject", "Test Body")));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void createAndBroadcastAppNotification_shouldSaveRepresentativeNotificationAndSendToTenantTopic() {
        // Arrange
        Map<String, Object> payload = new HashMap<>();
        payload.put("inAppTitle", "Broadcast Title");
        payload.put("inAppMessage", "This is a broadcast message!");

        NotificationRequest broadcastRequest = new NotificationRequest(
                BROADCAST_REQUEST_ID,
                null, // userId is null/ignored for broadcast trigger
                NotificationRequest.ChannelType.IN_APP,
                null,
                null, // No template for this simple test, or mock templatingService
                payload,
                null
        );

        long startTime = System.nanoTime();

        // Act
        AppNotification representativeNotification = appNotificationService.createAndBroadcastAppNotification(broadcastRequest);

        long endTime = System.nanoTime();
        long durationNanos = endTime - startTime;
        System.out.printf("Server-side broadcast initiation took: %.3f ms%n", durationNanos / 1_000_000.0);


        // Assert
        // 1. Verify that a representative notification was saved
        ArgumentCaptor<AppNotification> appNotificationCaptor = ArgumentCaptor.forClass(AppNotification.class);
        verify(appNotificationRepository, times(1)).save(appNotificationCaptor.capture());
        AppNotification savedEntity = appNotificationCaptor.getValue();

        assertNotNull(savedEntity);
        assertEquals(AppNotificationServiceImpl.BROADCAST_USER_ID_PLACEHOLDER, savedEntity.getUserId());
        assertEquals("Broadcast Title", savedEntity.getTitle());
        assertEquals(BROADCAST_REQUEST_ID, savedEntity.getNotificationRequestId());

        // 2. Verify that SimpMessagingTemplate.convertAndSend was called with the correct tenant topic and payload
        String expectedTopic = "/topic/in-app-notifications/" + TEST_TENANT_ID;
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate, times(1)).convertAndSend(eq(expectedTopic), payloadCaptor.capture());

        Object sentPayload = payloadCaptor.getValue();
        assertInstanceOf(AppNotification.class, sentPayload);
        AppNotification sentAppNotification = (AppNotification) sentPayload;
        assertEquals(AppNotificationServiceImpl.BROADCAST_USER_ID_PLACEHOLDER, sentAppNotification.getUserId());
        assertEquals("Broadcast Title", sentAppNotification.getTitle());

        // Log success
        System.out.println("Test createAndBroadcastAppNotification completed successfully.");
        System.out.println("Verified save to repository and send to topic: " + expectedTopic);
    }

    // You can add more tests for error conditions, different payloads, etc.
}