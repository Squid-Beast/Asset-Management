package com.example.asset_management.service;

import com.example.asset_management.model.AssetLoan;
import com.example.asset_management.model.OutboxEvent;
import com.example.asset_management.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void publishAssetAssignedEvent(AssetLoan loan) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("loanId", loan.getId());
            payload.put("assetId", loan.getAssetId());
            payload.put("userId", loan.getUserId());
            payload.put("assignedById", loan.getAssignedById());
            payload.put("dueAt", loan.getDueAt());
            payload.put("assignedAt", loan.getApprovedAt() != null ? loan.getApprovedAt() : loan.getRequestedAt());

            String payloadJson = objectMapper.writeValueAsString(payload);

            OutboxEvent event = new OutboxEvent();
            event.setAggregateType("ASSET_LOAN");
            event.setAggregateId(loan.getId());
            event.setEventType("AssetAssigned");
            event.setPayloadJson(payloadJson);
            event.setCreatedAt(LocalDateTime.now());
            event.setRetryCount(0);

            outboxEventRepository.save(event);
            log.info("AssetAssigned event queued for loan ID: {}", loan.getId());
        } catch (Exception e) {
            log.error("Failed to publish AssetAssigned event for loan ID: {}", loan.getId(), e);
        }
    }

    @Transactional
    public void publishAssetReturnedEvent(AssetLoan loan) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("loanId", loan.getId());
            payload.put("assetId", loan.getAssetId());
            payload.put("userId", loan.getUserId());
            payload.put("returnedAt", loan.getReturnedAt());
            payload.put("damageNote", loan.getDamageNote());

            String payloadJson = objectMapper.writeValueAsString(payload);

            OutboxEvent event = new OutboxEvent();
            event.setAggregateType("ASSET_LOAN");
            event.setAggregateId(loan.getId());
            event.setEventType("AssetReturned");
            event.setPayloadJson(payloadJson);
            event.setCreatedAt(LocalDateTime.now());
            event.setRetryCount(0);

            outboxEventRepository.save(event);
            log.info("AssetReturned event queued for loan ID: {}", loan.getId());
        } catch (Exception e) {
            log.error("Failed to publish AssetReturned event for loan ID: {}", loan.getId(), e);
        }
    }

    @Transactional
    public void publishAssetDueSoonEvent(AssetLoan loan) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("loanId", loan.getId());
            payload.put("assetId", loan.getAssetId());
            payload.put("userId", loan.getUserId());
            payload.put("dueAt", loan.getDueAt());

            String payloadJson = objectMapper.writeValueAsString(payload);

            OutboxEvent event = new OutboxEvent();
            event.setAggregateType("ASSET_LOAN");
            event.setAggregateId(loan.getId());
            event.setEventType("AssetDueSoon");
            event.setPayloadJson(payloadJson);
            event.setCreatedAt(LocalDateTime.now());
            event.setRetryCount(0);

            outboxEventRepository.save(event);
            log.info("AssetDueSoon event queued for loan ID: {}", loan.getId());
        } catch (Exception e) {
            log.error("Failed to publish AssetDueSoon event for loan ID: {}", loan.getId(), e);
        }
    }

    @Transactional
    public void publishAssetOverdueEvent(AssetLoan loan) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("loanId", loan.getId());
            payload.put("assetId", loan.getAssetId());
            payload.put("userId", loan.getUserId());
            payload.put("dueAt", loan.getDueAt());

            String payloadJson = objectMapper.writeValueAsString(payload);

            OutboxEvent event = new OutboxEvent();
            event.setAggregateType("ASSET_LOAN");
            event.setAggregateId(loan.getId());
            event.setEventType("AssetOverdue");
            event.setPayloadJson(payloadJson);
            event.setCreatedAt(LocalDateTime.now());
            event.setRetryCount(0);

            outboxEventRepository.save(event);
            log.info("AssetOverdue event queued for loan ID: {}", loan.getId());
        } catch (Exception e) {
            log.error("Failed to publish AssetOverdue event for loan ID: {}", loan.getId(), e);
        }
    }

    @Transactional
    public void publishAssetRejectedEvent(AssetLoan loan) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("loanId", loan.getId());
            payload.put("assetId", loan.getAssetId());
            payload.put("userId", loan.getUserId());
            payload.put("rejectedAt", LocalDateTime.now());

            String payloadJson = objectMapper.writeValueAsString(payload);

            OutboxEvent event = new OutboxEvent();
            event.setAggregateType("ASSET_LOAN");
            event.setAggregateId(loan.getId());
            event.setEventType("AssetRejected");
            event.setPayloadJson(payloadJson);
            event.setCreatedAt(LocalDateTime.now());
            event.setRetryCount(0);

            outboxEventRepository.save(event);
            log.info("AssetRejected event queued for loan ID: {}", loan.getId());
        } catch (Exception e) {
            log.error("Failed to publish AssetRejected event for loan ID: {}", loan.getId(), e);
        }
    }
}
