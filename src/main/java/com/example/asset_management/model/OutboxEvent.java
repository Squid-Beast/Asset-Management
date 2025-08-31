package com.example.asset_management.model;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "outbox_events")
public class OutboxEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "aggregate_type")
    private String aggregateType;
    @Column(name = "aggregate_id")
    private Long aggregateId;
    @Column(name = "event_type")
    private String eventType;
    @Column(name = "payload_json")
    private String payloadJson;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    @Column(name = "retry_count")
    private Integer retryCount;
}