package com.example.asset_management.model;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "asset_loans")
public class AssetLoan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "asset_id")
    private Long assetId;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "assigned_by_id")
    private Long assignedById;
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private LoanStatus status;
    @Column(name = "requested_at")
    private LocalDateTime requestedAt;
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    @Column(name = "due_at")
    private LocalDateTime dueAt;
    @Column(name = "returned_at")
    private LocalDateTime returnedAt;
    @Column(name = "damage_note")
    private String damageNote;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum LoanStatus {
        pending_approval,
        loaned,
        returned,
        rejected,
        overdue
    }
}