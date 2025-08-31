package com.example.asset_management.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarEvent {
    private Long id;
    private String title;
    private String description;
    private Long userId;
    private Long assetLoanId;
    private EventType eventType;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private EventStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Relationships
    private User user;
    private AssetLoan assetLoan;
    
    public enum EventType {
        ASSET_DUE,
        ASSET_OVERDUE,
        ASSET_RETURNED,
        REMINDER
    }
    
    public enum EventStatus {
        ACTIVE,
        COMPLETED,
        CANCELLED
    }
}
