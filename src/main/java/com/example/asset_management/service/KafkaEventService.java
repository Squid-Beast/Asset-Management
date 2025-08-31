package com.example.asset_management.service;

import com.example.asset_management.dto.KafkaEventPayload;
import com.example.asset_management.model.AssetLoan;
import com.example.asset_management.model.User;
import com.example.asset_management.model.Asset;
import com.example.asset_management.repository.UserRepository;
import com.example.asset_management.repository.AssetRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaEventService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final AssetRepository assetRepository;

    // Topic names
    private static final String ASSETS_EVENTS_TOPIC = "assets.events";
    private static final String NOTIFICATIONS_EMAIL_TOPIC = "notifications.email";
    private static final String NOTIFICATIONS_PUSH_TOPIC = "notifications.push";
    private static final String REALTIME_UPDATES_TOPIC = "realtime.updates";
    private static final String USER_ACTIVITY_TOPIC = "user.activity";

    public void publishAssetAssignedEvent(AssetLoan loan) {
        try {
            // Create event payload
            KafkaEventPayload eventPayload = createEventPayload(
                "AssetAssigned", 
                "ASSET_LOAN", 
                loan.getId(),
                createAssetAssignedData(loan)
            );

            // Publish to multiple topics
            publishToTopic(ASSETS_EVENTS_TOPIC, eventPayload);
            publishToTopic(REALTIME_UPDATES_TOPIC, eventPayload);
            
            // Trigger notification
            publishNotificationEvent(loan, "AssetAssigned");
            
            log.info("AssetAssigned event published for loan ID: {}", loan.getId());
            
        } catch (Exception e) {
            log.error("Failed to publish AssetAssigned event for loan ID: {}", loan.getId(), e);
        }
    }

    public void publishAssetReturnedEvent(AssetLoan loan) {
        try {
            KafkaEventPayload eventPayload = createEventPayload(
                "AssetReturned", 
                "ASSET_LOAN", 
                loan.getId(),
                createAssetReturnedData(loan)
            );

            publishToTopic(ASSETS_EVENTS_TOPIC, eventPayload);
            publishToTopic(REALTIME_UPDATES_TOPIC, eventPayload);
            
            publishNotificationEvent(loan, "AssetReturned");
            
            log.info("AssetReturned event published for loan ID: {}", loan.getId());
            
        } catch (Exception e) {
            log.error("Failed to publish AssetReturned event for loan ID: {}", loan.getId(), e);
        }
    }

    public void publishAssetDueSoonEvent(AssetLoan loan) {
        try {
            KafkaEventPayload eventPayload = createEventPayload(
                "AssetDueSoon", 
                "ASSET_LOAN", 
                loan.getId(),
                createAssetDueSoonData(loan)
            );

            publishToTopic(ASSETS_EVENTS_TOPIC, eventPayload);
            publishNotificationEvent(loan, "AssetDueSoon");
            
            log.info("AssetDueSoon event published for loan ID: {}", loan.getId());
            
        } catch (Exception e) {
            log.error("Failed to publish AssetDueSoon event for loan ID: {}", loan.getId(), e);
        }
    }

    public void publishAssetOverdueEvent(AssetLoan loan) {
        try {
            KafkaEventPayload eventPayload = createEventPayload(
                "AssetOverdue", 
                "ASSET_LOAN", 
                loan.getId(),
                createAssetOverdueData(loan)
            );

            publishToTopic(ASSETS_EVENTS_TOPIC, eventPayload);
            publishNotificationEvent(loan, "AssetOverdue");
            
            log.info("AssetOverdue event published for loan ID: {}", loan.getId());
            
        } catch (Exception e) {
            log.error("Failed to publish AssetOverdue event for loan ID: {}", loan.getId(), e);
        }
    }

    public void publishUserActivityEvent(String eventType, Long userId, Map<String, Object> activityData) {
        try {
            KafkaEventPayload eventPayload = createEventPayload(
                eventType, 
                "USER", 
                userId,
                activityData
            );

            publishToTopic(USER_ACTIVITY_TOPIC, eventPayload);
            
            log.info("User activity event published: {} for user: {}", eventType, userId);
            
        } catch (Exception e) {
            log.error("Failed to publish user activity event: {} for user: {}", eventType, userId, e);
        }
    }

    private void publishNotificationEvent(AssetLoan loan, String eventType) {
        try {
            User user = userRepository.findById(loan.getUserId()).orElse(null);
            Asset asset = assetRepository.findById(loan.getAssetId()).orElse(null);
            
            if (user != null && asset != null) {
                Map<String, Object> notificationData = new HashMap<>();
                notificationData.put("recipientEmail", user.getEmail());
                notificationData.put("recipientName", user.getFirstName() + " " + user.getLastName());
                notificationData.put("assetName", asset.getName());
                notificationData.put("assetTag", asset.getAssetTag());
                notificationData.put("dueAt", loan.getDueAt());
                notificationData.put("eventType", eventType);

                KafkaEventPayload notificationPayload = createEventPayload(
                    "NotificationRequest", 
                    "NOTIFICATION", 
                    loan.getId(),
                    notificationData
                );

                publishToTopic(NOTIFICATIONS_EMAIL_TOPIC, notificationPayload);
                publishToTopic(NOTIFICATIONS_PUSH_TOPIC, notificationPayload);
            }
            
        } catch (Exception e) {
            log.error("Failed to publish notification event for loan ID: {}", loan.getId(), e);
        }
    }

    private KafkaEventPayload createEventPayload(String eventType, String aggregateType, Long aggregateId, Map<String, Object> data) {
        KafkaEventPayload payload = new KafkaEventPayload();
        payload.setEventId(UUID.randomUUID().toString());
        payload.setEventType(eventType);
        payload.setTimestamp(LocalDateTime.now());
        payload.setAggregateType(aggregateType);
        payload.setAggregateId(aggregateId);
        payload.setData(data);
        payload.setSource("asset-management-api");
        payload.setVersion("1.0");
        
        Map<String, String> metadata = new HashMap<>();
        metadata.put("correlation-id", UUID.randomUUID().toString());
        metadata.put("tenant-id", "default");
        payload.setMetadata(metadata);
        
        return payload;
    }

    private void publishToTopic(String topicName, KafkaEventPayload payload) {
        try {
            String key = payload.getAggregateType() + "-" + payload.getAggregateId();
            String message = objectMapper.writeValueAsString(payload);
            
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topicName, key, message);
            
            future.whenComplete((result, exception) -> {
                if (exception == null) {
                    log.debug("Event sent successfully to topic {}: {} with offset {}",
                        topicName, payload.getEventType(), result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send event to topic {}: {}", topicName, payload.getEventType(), exception);
                }
            });
            
        } catch (Exception e) {
            log.error("Failed to serialize and publish event to topic {}: {}", topicName, payload.getEventType(), e);
        }
    }

    private Map<String, Object> createAssetAssignedData(AssetLoan loan) {
        Map<String, Object> data = new HashMap<>();
        data.put("loanId", loan.getId());
        data.put("assetId", loan.getAssetId());
        data.put("userId", loan.getUserId());
        data.put("assignedById", loan.getAssignedById());
        data.put("dueAt", loan.getDueAt());
        data.put("assignedAt", loan.getApprovedAt() != null ? loan.getApprovedAt() : loan.getRequestedAt());
        data.put("status", loan.getStatus().toString());
        return data;
    }

    private Map<String, Object> createAssetReturnedData(AssetLoan loan) {
        Map<String, Object> data = new HashMap<>();
        data.put("loanId", loan.getId());
        data.put("assetId", loan.getAssetId());
        data.put("userId", loan.getUserId());
        data.put("returnedAt", loan.getReturnedAt());
        data.put("damageNote", loan.getDamageNote());
        data.put("status", loan.getStatus().toString());
        return data;
    }

    private Map<String, Object> createAssetDueSoonData(AssetLoan loan) {
        Map<String, Object> data = new HashMap<>();
        data.put("loanId", loan.getId());
        data.put("assetId", loan.getAssetId());
        data.put("userId", loan.getUserId());
        data.put("dueAt", loan.getDueAt());
        data.put("daysUntilDue", java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), loan.getDueAt()));
        return data;
    }

    private Map<String, Object> createAssetOverdueData(AssetLoan loan) {
        Map<String, Object> data = new HashMap<>();
        data.put("loanId", loan.getId());
        data.put("assetId", loan.getAssetId());
        data.put("userId", loan.getUserId());
        data.put("dueAt", loan.getDueAt());
        data.put("daysPastDue", java.time.temporal.ChronoUnit.DAYS.between(loan.getDueAt(), LocalDateTime.now()));
        return data;
    }
}
