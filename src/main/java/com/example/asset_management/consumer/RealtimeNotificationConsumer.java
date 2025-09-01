package com.example.asset_management.consumer;

import com.example.asset_management.dto.KafkaEventPayload;
import com.example.asset_management.dto.NotificationMessage;
import com.example.asset_management.model.Asset;
import com.example.asset_management.model.User;
import com.example.asset_management.repository.AssetRepository;
import com.example.asset_management.repository.UserRepository;
import com.example.asset_management.service.WebSocketNotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
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
@Profile("!docker")
public class RealtimeNotificationConsumer {

    private final ObjectMapper objectMapper;
    private final WebSocketNotificationService webSocketNotificationService;
    private final UserRepository userRepository;
    private final AssetRepository assetRepository;

    @KafkaListener(topics = "realtime.updates", groupId = "realtime-notification-processor")
    public void handleRealtimeUpdate(
        @Payload String message,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment
    ) {
        try {
            log.info("Received realtime update from topic: {}, partition: {}, offset: {}", topic, partition, offset);
            
            KafkaEventPayload event = objectMapper.readValue(message, KafkaEventPayload.class);
            
            log.info("Processing realtime event: {} for aggregate: {} with ID: {}", 
                event.getEventType(), event.getAggregateType(), event.getAggregateId());
            
            // Process different event types and send appropriate notifications
            switch (event.getEventType()) {
                case "AssetAssigned":
                    handleAssetAssignedNotification(event);
                    break;
                case "AssetReturned":
                    handleAssetReturnedNotification(event);
                    break;
                case "AssetRejected":
                    handleAssetRejectedNotification(event);
                    break;
                case "AssetDueSoon":
                    handleAssetDueSoonNotification(event);
                    break;
                case "AssetOverdue":
                    handleAssetOverdueNotification(event);
                    break;
                case "ApprovalRequest":
                    handleApprovalRequestNotification(event);
                    break;
                case "ApprovalCompleted":
                    handleApprovalCompletedNotification(event);
                    break;
                case "RejectionCompleted":
                    handleRejectionCompletedNotification(event);
                    break;
                default:
                    log.warn("Unknown realtime event type: {}", event.getEventType());
            }
            
            // Acknowledge successful processing
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Failed to process realtime event: {}", message, e);
            // Don't acknowledge - message will be retried
            throw new RuntimeException("Realtime event processing failed", e);
        }
    }

    private void handleAssetAssignedNotification(KafkaEventPayload event) {
        try {
            Map<String, Object> data = event.getData();
            Long userId = Long.valueOf(data.get("userId").toString());
            Long assetId = Long.valueOf(data.get("assetId").toString());
            
            User user = userRepository.findById(userId).orElse(null);
            Asset asset = assetRepository.findById(assetId).orElse(null);
            
            if (user != null && asset != null) {
                String message = String.format("Asset '%s' has been assigned to you. Due date: %s", 
                    asset.getName(), data.get("dueAt"));
                
                webSocketNotificationService.sendAssetEventNotification(
                    user.getUsername(), 
                    "AssetAssigned", 
                    asset.getName(), 
                    message
                );
            }
        } catch (Exception e) {
            log.error("Failed to handle asset assigned notification", e);
        }
    }

    private void handleAssetReturnedNotification(KafkaEventPayload event) {
        try {
            Map<String, Object> data = event.getData();
            Long userId = Long.valueOf(data.get("userId").toString());
            Long assetId = Long.valueOf(data.get("assetId").toString());
            
            User user = userRepository.findById(userId).orElse(null);
            Asset asset = assetRepository.findById(assetId).orElse(null);
            
            if (user != null && asset != null) {
                String message = String.format("Asset '%s' has been returned successfully", asset.getName());
                
                webSocketNotificationService.sendAssetEventNotification(
                    user.getUsername(), 
                    "AssetReturned", 
                    asset.getName(), 
                    message
                );
            }
        } catch (Exception e) {
            log.error("Failed to handle asset returned notification", e);
        }
    }

    private void handleAssetRejectedNotification(KafkaEventPayload event) {
        try {
            Map<String, Object> data = event.getData();
            Long userId = Long.valueOf(data.get("userId").toString());
            Long assetId = Long.valueOf(data.get("assetId").toString());
            
            User user = userRepository.findById(userId).orElse(null);
            Asset asset = assetRepository.findById(assetId).orElse(null);
            
            if (user != null && asset != null) {
                String message = String.format("Your request for asset '%s' has been rejected", asset.getName());
                
                webSocketNotificationService.sendLoanStatusNotification(
                    user.getUsername(), 
                    "Rejected", 
                    asset.getName(), 
                    message
                );
            }
        } catch (Exception e) {
            log.error("Failed to handle asset rejected notification", e);
        }
    }

    private void handleAssetDueSoonNotification(KafkaEventPayload event) {
        try {
            Map<String, Object> data = event.getData();
            Long userId = Long.valueOf(data.get("userId").toString());
            Long assetId = Long.valueOf(data.get("assetId").toString());
            
            User user = userRepository.findById(userId).orElse(null);
            Asset asset = assetRepository.findById(assetId).orElse(null);
            
            if (user != null && asset != null) {
                String message = String.format("Asset '%s' is due soon on %s", 
                    asset.getName(), data.get("dueAt"));
                
                webSocketNotificationService.sendAssetEventNotification(
                    user.getUsername(), 
                    "AssetDueSoon", 
                    asset.getName(), 
                    message
                );
            }
        } catch (Exception e) {
            log.error("Failed to handle asset due soon notification", e);
        }
    }

    private void handleAssetOverdueNotification(KafkaEventPayload event) {
        try {
            Map<String, Object> data = event.getData();
            Long userId = Long.valueOf(data.get("userId").toString());
            Long assetId = Long.valueOf(data.get("assetId").toString());
            
            User user = userRepository.findById(userId).orElse(null);
            Asset asset = assetRepository.findById(assetId).orElse(null);
            
            if (user != null && asset != null) {
                String message = String.format("Asset '%s' is overdue since %s", 
                    asset.getName(), data.get("dueAt"));
                
                webSocketNotificationService.sendAssetEventNotification(
                    user.getUsername(), 
                    "AssetOverdue", 
                    asset.getName(), 
                    message
                );
            }
        } catch (Exception e) {
            log.error("Failed to handle asset overdue notification", e);
        }
    }

    private void handleApprovalRequestNotification(KafkaEventPayload event) {
        try {
            Map<String, Object> data = event.getData();
            String requesterName = (String) data.get("requesterName");
            String assetName = (String) data.get("assetName");
            
            // Send notification to all managers
            webSocketNotificationService.sendApprovalRequestNotification(requesterName, assetName);
            
        } catch (Exception e) {
            log.error("Failed to handle approval request notification", e);
        }
    }

    private void handleApprovalCompletedNotification(KafkaEventPayload event) {
        try {
            Map<String, Object> data = event.getData();
            String approverName = (String) data.get("approverName");
            String requesterName = (String) data.get("requesterName");
            String assetName = (String) data.get("assetName");
            
            // Send notification to all managers about the approval
            webSocketNotificationService.sendNotificationToManagers(
                NotificationMessage.builder()
                    .type("APPROVAL_COMPLETED")
                    .title("Loan Approved")
                    .message(String.format("%s approved %s's request for asset '%s'", 
                        approverName, requesterName, assetName))
                    .severity("success")
                    .build()
            );
            
        } catch (Exception e) {
            log.error("Failed to handle approval completed notification", e);
        }
    }

    private void handleRejectionCompletedNotification(KafkaEventPayload event) {
        try {
            Map<String, Object> data = event.getData();
            String rejectorName = (String) data.get("rejectorName");
            String requesterName = (String) data.get("requesterName");
            String assetName = (String) data.get("assetName");
            
            // Send notification to all managers about the rejection
            webSocketNotificationService.sendNotificationToManagers(
                NotificationMessage.builder()
                    .type("REJECTION_COMPLETED")
                    .title("Loan Rejected")
                    .message(String.format("%s rejected %s's request for asset '%s'", 
                        rejectorName, requesterName, assetName))
                    .severity("warning")
                    .build()
            );
            
        } catch (Exception e) {
            log.error("Failed to handle rejection completed notification", e);
        }
    }
}
