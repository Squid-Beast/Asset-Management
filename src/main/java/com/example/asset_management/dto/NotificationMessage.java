package com.example.asset_management.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class NotificationMessage {
    private String id;
    private String type;
    private String title;
    private String message;
    private Map<String, Object> data;
    private LocalDateTime timestamp;
    private String userId;
    private String severity; // info, success, warning, error
    
    public NotificationMessage() {
        this.timestamp = LocalDateTime.now();
        this.id = java.util.UUID.randomUUID().toString();
    }
    
    public NotificationMessage(String id, String type, String title, String message, 
                             Map<String, Object> data, LocalDateTime timestamp, 
                             String userId, String severity) {
        this.id = id != null ? id : java.util.UUID.randomUUID().toString();
        this.type = type;
        this.title = title;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.userId = userId;
        this.severity = severity != null ? severity : "info";
    }
}
