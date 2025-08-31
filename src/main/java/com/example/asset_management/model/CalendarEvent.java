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
@Table(name = "calendar_events")
public class CalendarEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "title")
    private String title;
    @Column(name = "description")
    private String description;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "asset_loan_id")
    private Long assetLoanId;
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type")
    private EventType eventType;
    @Column(name = "start_at")
    private LocalDateTime startAt;
    @Column(name = "end_at")
    private LocalDateTime endAt;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EventStatus status;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum EventType {
        asset_due,
        asset_overdue,
        asset_returned,
        reminder
    }
    
    public enum EventStatus {
        active,
        completed,
        cancelled
    }
}