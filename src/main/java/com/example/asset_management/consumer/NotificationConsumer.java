package com.example.asset_management.consumer;

import com.example.asset_management.dto.KafkaEventPayload;
import com.example.asset_management.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    @KafkaListener(topics = "notifications.email", groupId = "notification-email-processor")
    public void handleEmailNotification(
        @Payload String message,
        @Header Map<String, Object> headers,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
        Acknowledgment acknowledgment
    ) {
        try {
            log.info("Received email notification from topic: {}", topic);
            
            KafkaEventPayload event = objectMapper.readValue(message, KafkaEventPayload.class);
            
            if ("NotificationRequest".equals(event.getEventType())) {
                processEmailNotification(event);
            }
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Failed to process email notification: {}", message, e);
            throw new RuntimeException("Email notification processing failed", e);
        }
    }

    @KafkaListener(topics = "notifications.push", groupId = "notification-push-processor")
    public void handlePushNotification(
        @Payload String message,
        @Header Map<String, Object> headers,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
        Acknowledgment acknowledgment
    ) {
        try {
            log.info("Received push notification from topic: {}", topic);
            
            KafkaEventPayload event = objectMapper.readValue(message, KafkaEventPayload.class);
            
            if ("NotificationRequest".equals(event.getEventType())) {
                processPushNotification(event);
            }
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Failed to process push notification: {}", message, e);
            throw new RuntimeException("Push notification processing failed", e);
        }
    }

    @KafkaListener(topics = "notifications.sms", groupId = "notification-sms-processor")
    public void handleSmsNotification(
        @Payload String message,
        @Header Map<String, Object> headers,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
        Acknowledgment acknowledgment
    ) {
        try {
            log.info("Received SMS notification from topic: {}", topic);
            
            KafkaEventPayload event = objectMapper.readValue(message, KafkaEventPayload.class);
            
            if ("NotificationRequest".equals(event.getEventType())) {
                processSmsNotification(event);
            }
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Failed to process SMS notification: {}", message, e);
            throw new RuntimeException("SMS notification processing failed", e);
        }
    }

    private void processEmailNotification(KafkaEventPayload event) {
        try {
            Map<String, Object> data = event.getData();
            String recipientEmail = (String) data.get("recipientEmail");
            String recipientName = (String) data.get("recipientName");
            String assetName = (String) data.get("assetName");
            String eventType = (String) data.get("eventType");
            
            log.info("Processing email notification for: {} ({}), Event: {}, Asset: {}", 
                recipientName, recipientEmail, eventType, assetName);
            
            // Route to appropriate notification method based on event type
            switch (eventType) {
                case "AssetAssigned":
                    notificationService.sendAssetAssignedNotification(
                        recipientEmail, assetName, data.get("dueAt").toString());
                    break;
                case "AssetReturned":
                    notificationService.sendAssetReturnedNotification(recipientEmail, assetName);
                    break;
                case "AssetDueSoon":
                    notificationService.sendAssetDueSoonNotification(
                        recipientEmail, assetName, data.get("dueAt").toString());
                    break;
                case "AssetOverdue":
                    notificationService.sendAssetOverdueNotification(
                        recipientEmail, assetName, data.get("dueAt").toString());
                    break;
                default:
                    log.warn("Unknown notification event type: {}", eventType);
            }
            
        } catch (Exception e) {
            log.error("Failed to process email notification", e);
            throw e;
        }
    }

    private void processPushNotification(KafkaEventPayload event) {
        try {
            Map<String, Object> data = event.getData();
            String recipientName = (String) data.get("recipientName");
            String assetName = (String) data.get("assetName");
            String eventType = (String) data.get("eventType");
            
            log.info("Processing push notification for: {}, Event: {}, Asset: {}", 
                recipientName, eventType, assetName);
            
            // Here you would integrate with push notification service
            // For now, just log the action
            log.info("PUSH NOTIFICATION: {} - {} for asset '{}'", 
                recipientName, eventType, assetName);
            
        } catch (Exception e) {
            log.error("Failed to process push notification", e);
            throw e;
        }
    }

    private void processSmsNotification(KafkaEventPayload event) {
        try {
            Map<String, Object> data = event.getData();
            String recipientName = (String) data.get("recipientName");
            String assetName = (String) data.get("assetName");
            String eventType = (String) data.get("eventType");
            
            log.info("Processing SMS notification for: {}, Event: {}, Asset: {}", 
                recipientName, eventType, assetName);
            
            // Here you would integrate with SMS service
            // For now, just log the action
            log.info("SMS NOTIFICATION: {} - {} for asset '{}'", 
                recipientName, eventType, assetName);
            
        } catch (Exception e) {
            log.error("Failed to process SMS notification", e);
            throw e;
        }
    }
}
