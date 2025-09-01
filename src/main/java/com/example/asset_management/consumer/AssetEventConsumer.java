package com.example.asset_management.consumer;

import com.example.asset_management.dto.KafkaEventPayload;
import com.example.asset_management.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!docker")
public class AssetEventConsumer {

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    @KafkaListener(topics = "assets.events", groupId = "asset-management-group")
    public void handleAssetEvent(
        @Payload String message,
        @Headers Map<String, Object> headers,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment
    ) {
        try {
            log.info("Received message from topic: {}, partition: {}, offset: {}", topic, partition, offset);
            
            KafkaEventPayload event = objectMapper.readValue(message, KafkaEventPayload.class);
            
            log.info("Processing asset event: {} for aggregate: {} with ID: {}", 
                event.getEventType(), event.getAggregateType(), event.getAggregateId());
            
            switch (event.getEventType()) {
                case "AssetAssigned":
                    handleAssetAssigned(event);
                    break;
                case "AssetReturned":
                    handleAssetReturned(event);
                    break;
                case "AssetDueSoon":
                    handleAssetDueSoon(event);
                    break;
                case "AssetOverdue":
                    handleAssetOverdue(event);
                    break;
                default:
                    log.warn("Unknown asset event type: {}", event.getEventType());
            }
            
            // Acknowledge successful processing
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Failed to process asset event: {}", message, e);
            // Don't acknowledge - message will be retried
            throw new RuntimeException("Asset event processing failed", e);
        }
    }

    @KafkaListener(topics = "assets.dlq", groupId = "asset-management-dlq-processor")
    public void handleFailedAssetEvent(
        @Payload String message,
        @Headers Map<String, Object> headers,
        Acknowledgment acknowledgment
    ) {
        try {
            log.error("Processing failed asset event from DLQ: {}", message);
            
            // Handle failed events - could be:
            // 1. Log to database for manual review
            // 2. Send alert to administrators
            // 3. Attempt alternative processing
            
            // For now, just log the failure
            log.error("DLQ Event requires manual intervention: {}", message);
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Failed to process DLQ event: {}", message, e);
        }
    }

    private void handleAssetAssigned(KafkaEventPayload event) {
        try {
            Map<String, Object> data = event.getData();
            log.info("Asset assigned - Loan ID: {}, Asset ID: {}, User ID: {}", 
                data.get("loanId"), data.get("assetId"), data.get("userId"));
            
            // Additional processing can be added here
            // e.g., update analytics, trigger workflows, etc.
            
        } catch (Exception e) {
            log.error("Failed to handle AssetAssigned event", e);
            throw e;
        }
    }

    private void handleAssetReturned(KafkaEventPayload event) {
        try {
            Map<String, Object> data = event.getData();
            log.info("Asset returned - Loan ID: {}, Asset ID: {}, User ID: {}", 
                data.get("loanId"), data.get("assetId"), data.get("userId"));
            
            // Additional processing can be added here
            
        } catch (Exception e) {
            log.error("Failed to handle AssetReturned event", e);
            throw e;
        }
    }

    private void handleAssetDueSoon(KafkaEventPayload event) {
        try {
            Map<String, Object> data = event.getData();
            log.info("Asset due soon - Loan ID: {}, Asset ID: {}, User ID: {}, Days until due: {}", 
                data.get("loanId"), data.get("assetId"), data.get("userId"), data.get("daysUntilDue"));
            
            // Additional processing can be added here
            
        } catch (Exception e) {
            log.error("Failed to handle AssetDueSoon event", e);
            throw e;
        }
    }

    private void handleAssetOverdue(KafkaEventPayload event) {
        try {
            Map<String, Object> data = event.getData();
            log.info("Asset overdue - Loan ID: {}, Asset ID: {}, User ID: {}, Days past due: {}", 
                data.get("loanId"), data.get("assetId"), data.get("userId"), data.get("daysPastDue"));
            
            // Additional processing can be added here
            
        } catch (Exception e) {
            log.error("Failed to handle AssetOverdue event", e);
            throw e;
        }
    }
}
