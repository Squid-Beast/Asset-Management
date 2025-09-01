package com.example.asset_management.consumer;

import com.example.asset_management.dto.KafkaEventPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class RealtimeUpdateConsumer {

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "realtime.updates", groupId = "realtime-dashboard-updates")
    public void handleRealtimeUpdate(
        @Payload String message,
        @Headers Map<String, Object> headers,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
        Acknowledgment acknowledgment
    ) {
        try {
            log.info("Received realtime update from topic: {}", topic);
            
            KafkaEventPayload event = objectMapper.readValue(message, KafkaEventPayload.class);
            
            log.info("Processing realtime update: {} for aggregate: {} with ID: {}", 
                event.getEventType(), event.getAggregateType(), event.getAggregateId());
            
            processRealtimeUpdate(event);
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Failed to process realtime update: {}", message, e);
            throw new RuntimeException("Realtime update processing failed", e);
        }
    }

    @KafkaListener(topics = "realtime.status", groupId = "realtime-status-updates")
    public void handleRealtimeStatus(
        @Payload String message,
        @Headers Map<String, Object> headers,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
        Acknowledgment acknowledgment
    ) {
        try {
            log.info("Received status update from topic: {}", topic);
            
            KafkaEventPayload event = objectMapper.readValue(message, KafkaEventPayload.class);
            
            log.info("Processing status update: {} for aggregate: {} with ID: {}", 
                event.getEventType(), event.getAggregateType(), event.getAggregateId());
            
            processStatusUpdate(event);
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Failed to process status update: {}", message, e);
            throw new RuntimeException("Status update processing failed", e);
        }
    }

    private void processRealtimeUpdate(KafkaEventPayload event) {
        try {
            Map<String, Object> data = event.getData();
            
            // Here you would typically:
            // 1. Send WebSocket messages to connected frontend clients
            // 2. Update cached dashboard data
            // 3. Trigger real-time UI updates
            
            log.info("REALTIME UPDATE: {} - Data: {}", event.getEventType(), data);
            
            // Example: WebSocket message to dashboard
            // webSocketService.sendToClients("/topic/dashboard", event);
            
        } catch (Exception e) {
            log.error("Failed to process realtime update", e);
            throw e;
        }
    }

    private void processStatusUpdate(KafkaEventPayload event) {
        try {
            Map<String, Object> data = event.getData();
            
            log.info("STATUS UPDATE: {} - Data: {}", event.getEventType(), data);
            
            // Here you would update status indicators, badges, etc.
            
        } catch (Exception e) {
            log.error("Failed to process status update", e);
            throw e;
        }
    }
}
