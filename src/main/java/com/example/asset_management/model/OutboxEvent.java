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
public class OutboxEvent {
    private Long id;
    private String aggregateType;
    private Long aggregateId;
    private String eventType;
    private String payloadJson;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private Integer retryCount;
}
