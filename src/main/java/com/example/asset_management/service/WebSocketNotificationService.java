package com.example.asset_management.service;

import com.example.asset_management.dto.NotificationMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Send notification to a specific user
     */
    public void sendNotificationToUser(String username, NotificationMessage notification) {
        try {
            String destination = "/user/" + username + "/queue/notifications";
            messagingTemplate.convertAndSendToUser(username, "/queue/notifications", notification);
            log.info("Sent notification to user {}: {}", username, notification.getMessage());
        } catch (Exception e) {
            log.error("Failed to send notification to user {}: {}", username, e.getMessage());
        }
    }

    /**
     * Send notification to all users (broadcast)
     */
    public void sendNotificationToAll(NotificationMessage notification) {
        try {
            messagingTemplate.convertAndSend("/topic/notifications", notification);
            log.info("Sent broadcast notification: {}", notification.getMessage());
        } catch (Exception e) {
            log.error("Failed to send broadcast notification: {}", e.getMessage());
        }
    }

    /**
     * Send notification to managers only
     */
    public void sendNotificationToManagers(NotificationMessage notification) {
        try {
            messagingTemplate.convertAndSend("/topic/manager-notifications", notification);
            log.info("Sent manager notification: {}", notification.getMessage());
        } catch (Exception e) {
            log.error("Failed to send manager notification: {}", e.getMessage());
        }
    }

    /**
     * Send asset event notification
     */
    public void sendAssetEventNotification(String username, String eventType, String assetName, String message) {
        NotificationMessage notification = NotificationMessage.builder()
                .type("ASSET_EVENT")
                .title(eventType)
                .message(message)
                .data(java.util.Map.of(
                    "eventType", eventType,
                    "assetName", assetName,
                    "timestamp", java.time.LocalDateTime.now().toString()
                ))
                .build();
        
        sendNotificationToUser(username, notification);
    }

    /**
     * Send loan status change notification
     */
    public void sendLoanStatusNotification(String username, String status, String assetName, String message) {
        NotificationMessage notification = NotificationMessage.builder()
                .type("LOAN_STATUS")
                .title("Loan " + status)
                .message(message)
                .data(java.util.Map.of(
                    "status", status,
                    "assetName", assetName,
                    "timestamp", java.time.LocalDateTime.now().toString()
                ))
                .build();
        
        sendNotificationToUser(username, notification);
    }

    /**
     * Send approval request notification to managers
     */
    public void sendApprovalRequestNotification(String requesterName, String assetName) {
        NotificationMessage notification = NotificationMessage.builder()
                .type("APPROVAL_REQUEST")
                .title("New Approval Request")
                .message(requesterName + " has requested approval for " + assetName)
                .data(java.util.Map.of(
                    "requesterName", requesterName,
                    "assetName", assetName,
                    "timestamp", java.time.LocalDateTime.now().toString()
                ))
                .build();
        
        sendNotificationToManagers(notification);
    }
}
