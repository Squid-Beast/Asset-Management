package com.example.asset_management.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
public class KafkaEventPayload {
    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private String aggregateType;
    private Long aggregateId;
    private Map<String, Object> data;
    private Map<String, String> metadata;
    private String source;
    private String version;
}
