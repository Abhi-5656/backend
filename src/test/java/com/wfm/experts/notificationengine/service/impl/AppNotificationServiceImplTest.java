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

import java.lang.reflect.Field; // For reflection
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppNotificationServiceImplTest {

    @Mock
    private AppNotificationRepository appNotificationRepository;

    @Mock
    private TemplatingService templatingService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private AppNotificationServiceImpl appNotificationService;

    private final String TEST_TENANT_ID = "test-tenant";
    private final String BROADCAST_REQUEST_ID = UUID.randomUUID().toString();
    private final AtomicLong mockIdCounter = new AtomicLong(1);
    private final String DEFAULT_TEST_LANGUAGE = "en-US"; // Define a consistent default for tests

    @BeforeEach
    void setUp() {
        TenantContext.setTenant(TEST_TENANT_ID);
        mockIdCounter.set(1);

        // Simulate ID generation by JPA and return the entity with an ID
        when(appNotificationRepository.save(any(AppNotification.class)))
                .thenAnswer(invocation -> {
                    AppNotification appNotif = invocation.getArgument(0);
                    if (appNotif.getId() == null) {
                        appNotif.setId(mockIdCounter.getAndIncrement());
                    }
                    return appNotif;
                });

        // Manually set the @Value-annotated field 'defaultInAppLanguage' on the service instance for this test
        // This is important because @Value might not be processed in a pure unit test without full Spring context.
        try {
            Field field = AppNotificationServiceImpl.class.getDeclaredField("defaultInAppLanguage");
            field.setAccessible(true);
            field.set(appNotificationService, DEFAULT_TEST_LANGUAGE);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // This might happen if the field name changes. Consider a more robust way if this becomes an issue,
            // or ensure this test runs with a Spring context that populates @Value fields.
            System.err.println("Warning: Could not set defaultInAppLanguage via reflection. Test might rely on hardcoded default in service if @Value fails.");
            e.printStackTrace();
        }

        // DO NOT set up a global lenient().when(templatingService.getAndRenderTemplate(...)) here.
        // Define stubs specifically within each test that needs it.
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void createAndBroadcastAppNotification_shouldSaveRepresentativeNotificationAndSendToTenantTopic_withDirectPayload() {
        // Arrange
        Map<String, Object> payload = new HashMap<>();
        payload.put("inAppTitle", "Broadcast Title Direct");
        payload.put("inAppMessage", "This is a broadcast message (direct payload)!");

        NotificationRequest broadcastRequest = new NotificationRequest(
                BROADCAST_REQUEST_ID,
                null,
                NotificationRequest.ChannelType.IN_APP,
                null,
                null, // templateId is null
                payload,
                null
        );

        long startTime = System.nanoTime();

        // Act
        AppNotification representativeNotification = appNotificationService.createAndBroadcastAppNotification(broadcastRequest);

        long endTime = System.nanoTime();
        long durationNanos = endTime - startTime;
        System.out.printf("Test: Direct Payload Broadcast | Server-side initiation took: %.3f ms%n", durationNanos / 1_000_000.0);

        // Assert
        ArgumentCaptor<AppNotification> appNotificationCaptor = ArgumentCaptor.forClass(AppNotification.class);
        verify(appNotificationRepository, times(1)).save(appNotificationCaptor.capture());
        AppNotification savedEntity = appNotificationCaptor.getValue();

        assertNotNull(savedEntity);
        assertNotNull(savedEntity.getId(), "Saved entity should have an ID from mock");
        assertEquals(AppNotificationServiceImpl.BROADCAST_USER_ID_PLACEHOLDER, savedEntity.getUserId());
        assertEquals("Broadcast Title Direct", savedEntity.getTitle());
        assertEquals("This is a broadcast message (direct payload)!", savedEntity.getMessageBody());
        assertEquals(BROADCAST_REQUEST_ID, savedEntity.getNotificationRequestId());

        String expectedTopic = "/topic/in-app-notifications/" + TEST_TENANT_ID;
        ArgumentCaptor<Object> payloadWebSocketCaptor = ArgumentCaptor.forClass(Object.class); // Renamed for clarity
        verify(messagingTemplate, times(1)).convertAndSend(eq(expectedTopic), payloadWebSocketCaptor.capture());

        Object sentPayload = payloadWebSocketCaptor.getValue();
        assertTrue(sentPayload instanceof AppNotification);
        AppNotification sentAppNotification = (AppNotification) sentPayload;
        assertEquals(AppNotificationServiceImpl.BROADCAST_USER_ID_PLACEHOLDER, sentAppNotification.getUserId());
        assertEquals("Broadcast Title Direct", sentAppNotification.getTitle());
        assertNotNull(sentAppNotification.getId(), "Sent notification should reflect the saved ID");
        assertEquals(savedEntity.getId(), sentAppNotification.getId(), "Sent notification ID should match saved ID");

        System.out.println("Test createAndBroadcastAppNotification (direct payload) completed successfully.");
        System.out.println("Saved entity ID: " + savedEntity.getId());
        System.out.println("Verified save to repository and send to topic: " + expectedTopic);

        // Verify templatingService was NOT called
        verify(templatingService, never()).getAndRenderTemplate(anyString(), any(NotificationRequest.ChannelType.class), anyString(), anyMap());
    }

    @Test
    void createAndBroadcastAppNotification_shouldSaveRepresentativeNotificationAndSendToTenantTopic_withTemplate() {
        // Arrange
        String templateIdToUse = "test-broadcast-template";
        Map<String, Object> payloadForTemplate = new HashMap<>();
        payloadForTemplate.put("userName", "All Valued Users");

        // Scenario 1: No language in metadata, should use default (DEFAULT_TEST_LANGUAGE)
        Map<String, String> metadataWithoutLanguage = new HashMap<>();
        NotificationRequest broadcastRequestDefaultLang = new NotificationRequest(
                BROADCAST_REQUEST_ID + "-template-default",
                null,
                NotificationRequest.ChannelType.IN_APP,
                null,
                templateIdToUse,
                payloadForTemplate,
                metadataWithoutLanguage
        );

        // --- Mocking for default language scenario ---
        when(templatingService.getAndRenderTemplate(
                eq(templateIdToUse),
                eq(NotificationRequest.ChannelType.IN_APP),
                eq(DEFAULT_TEST_LANGUAGE), // Expecting the default language set in setUp via reflection
                eq(payloadForTemplate)
        ))
                .thenReturn(Optional.of(new TemplatingService.RenderedTemplateContent("Template Subject Default Lang", "Template Body Default Lang")));

        System.out.println("\nTesting broadcast with template (default language path)...");
        long startTimeDefault = System.nanoTime();
        // Act (default language)
        AppNotification representativeNotificationDefaultLang = appNotificationService.createAndBroadcastAppNotification(broadcastRequestDefaultLang);
        long endTimeDefault = System.nanoTime();
        System.out.printf("Test: Template Broadcast (Default Lang) | Server-side initiation took: %.3f ms%n", (endTimeDefault - startTimeDefault) / 1_000_000.0);


        // Assert (default language)
        ArgumentCaptor<AppNotification> appNotificationCaptorDefault = ArgumentCaptor.forClass(AppNotification.class);
        verify(appNotificationRepository, times(1)).save(appNotificationCaptorDefault.capture()); // First save
        AppNotification savedEntityDefault = appNotificationCaptorDefault.getValue();

        assertEquals("Template Subject Default Lang", savedEntityDefault.getTitle());
        assertEquals("Template Body Default Lang", savedEntityDefault.getMessageBody());
        verify(templatingService, times(1)).getAndRenderTemplate(
                eq(templateIdToUse),
                eq(NotificationRequest.ChannelType.IN_APP),
                eq(DEFAULT_TEST_LANGUAGE),
                eq(payloadForTemplate)
        );
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/in-app-notifications/" + TEST_TENANT_ID), eq(representativeNotificationDefaultLang));
        System.out.println("Broadcast with template (default language path) successful. Saved ID: " + savedEntityDefault.getId());


        // Scenario 2: Specific language in metadata
        Map<String, String> metadataWithLanguage = new HashMap<>();
        String specificTestLanguage = "fr-FR";
        metadataWithLanguage.put("language", specificTestLanguage);

        NotificationRequest broadcastRequestSpecificLang = new NotificationRequest(
                BROADCAST_REQUEST_ID + "-template-specific",
                null,
                NotificationRequest.ChannelType.IN_APP,
                null,
                templateIdToUse,
                payloadForTemplate,
                metadataWithLanguage
        );

        // --- Mocking for specific language scenario ---
        when(templatingService.getAndRenderTemplate(
                eq(templateIdToUse),
                eq(NotificationRequest.ChannelType.IN_APP),
                eq(specificTestLanguage),
                eq(payloadForTemplate)
        ))
                .thenReturn(Optional.of(new TemplatingService.RenderedTemplateContent("Template Subject Specific Lang", "Template Body Specific Lang")));


        System.out.println("\nTesting broadcast with template (specific language path)...");
        long startTimeSpecific = System.nanoTime();
        // Act (specific language)
        AppNotification representativeNotificationSpecificLang = appNotificationService.createAndBroadcastAppNotification(broadcastRequestSpecificLang);
        long endTimeSpecific = System.nanoTime();
        System.out.printf("Test: Template Broadcast (Specific Lang) | Server-side initiation took: %.3f ms%n", (endTimeSpecific - startTimeSpecific) / 1_000_000.0);

        // Assert (specific language)
        ArgumentCaptor<AppNotification> appNotificationCaptorSpecific = ArgumentCaptor.forClass(AppNotification.class);
        verify(appNotificationRepository, times(2)).save(appNotificationCaptorSpecific.capture()); // Second save
        AppNotification savedEntitySpecific = appNotificationCaptorSpecific.getValue();

        assertEquals("Template Subject Specific Lang", savedEntitySpecific.getTitle());
        assertEquals("Template Body Specific Lang", savedEntitySpecific.getMessageBody());
        verify(templatingService, times(1)).getAndRenderTemplate( // This specific mock invocation
                eq(templateIdToUse),
                eq(NotificationRequest.ChannelType.IN_APP),
                eq(specificTestLanguage),
                eq(payloadForTemplate)
        );
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/in-app-notifications/" + TEST_TENANT_ID), eq(representativeNotificationSpecificLang));
        System.out.println("Broadcast with template (specific language path) successful. Saved ID: " + savedEntitySpecific.getId());

        // Verify total calls after both scenarios in this test method
        verify(messagingTemplate, times(2)).convertAndSend(eq("/topic/in-app-notifications/" + TEST_TENANT_ID), any(AppNotification.class));
    }

    @Test
    void createAppNotification_shouldSaveNotificationAndSendToUserSpecificQueue() {
        // Arrange
        String targetUserId = "user123";
        String individualRequestId = UUID.randomUUID().toString();
        Map<String, Object> payload = new HashMap<>();
        payload.put("inAppTitle", "Individual Title");
        payload.put("inAppMessage", "This is for user123!");

        NotificationRequest individualRequest = new NotificationRequest(
                individualRequestId,
                targetUserId,
                NotificationRequest.ChannelType.IN_APP,
                null,
                null,
                payload,
                null
        );
        long startTime = System.nanoTime();
        // Act
        AppNotification resultNotification = appNotificationService.createAppNotification(individualRequest);
        long endTime = System.nanoTime();
        long durationNanos = endTime - startTime;
        System.out.printf("Test: Individual Notification | Server-side initiation took: %.3f ms%n", durationNanos / 1_000_000.0);


        // Assert
        ArgumentCaptor<AppNotification> appNotificationCaptor = ArgumentCaptor.forClass(AppNotification.class);
        verify(appNotificationRepository, times(1)).save(appNotificationCaptor.capture());
        AppNotification savedEntity = appNotificationCaptor.getValue();

        assertNotNull(savedEntity);
        assertNotNull(savedEntity.getId());
        assertEquals(targetUserId, savedEntity.getUserId());
        assertEquals("Individual Title", savedEntity.getTitle());
        assertEquals(individualRequestId, savedEntity.getNotificationRequestId());

        ArgumentCaptor<Object> payloadWebSocketCaptor = ArgumentCaptor.forClass(Object.class); // Renamed
        String expectedUserDestination = "/queue/in-app-notifications";
        verify(messagingTemplate, times(1)).convertAndSendToUser(eq(targetUserId), eq(expectedUserDestination), payloadWebSocketCaptor.capture());

        Object sentPayload = payloadWebSocketCaptor.getValue();
        assertTrue(sentPayload instanceof AppNotification);
        AppNotification sentAppNotification = (AppNotification) sentPayload;
        assertEquals(targetUserId, sentAppNotification.getUserId());
        assertEquals("Individual Title", sentAppNotification.getTitle());
        assertEquals(savedEntity.getId(), sentAppNotification.getId());

        System.out.println("Test createAppNotification (individual user) completed successfully.");
        System.out.println("Saved entity ID: " + savedEntity.getId());
        System.out.println("Verified save to repository and send to user: " + targetUserId + " on destination " + expectedUserDestination);
        verify(templatingService, never()).getAndRenderTemplate(anyString(), any(NotificationRequest.ChannelType.class), anyString(), anyMap());
    }
}