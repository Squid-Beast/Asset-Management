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
public class AssetLoan {
    private Long id;
    private Long assetId;
    private Long userId;
    private Long assignedById;
    private LoanStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime dueAt;
    private LocalDateTime returnedAt;
    private String damageNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Relationships
    private Asset asset;
    private User user;
    private User assignedBy;
    
    public enum LoanStatus {
        PENDING_APPROVAL,
        LOANED,
        RETURNED,
        OVERDUE
    }
}
