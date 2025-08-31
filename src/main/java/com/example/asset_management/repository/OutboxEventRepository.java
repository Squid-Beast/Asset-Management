package com.example.asset_management.repository;

import com.example.asset_management.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    
    @Query("SELECT oe FROM OutboxEvent oe WHERE oe.sentAt IS NULL ORDER BY oe.createdAt ASC")
    List<OutboxEvent> findUnsentEvents();
    
    @Query("SELECT oe FROM OutboxEvent oe WHERE oe.sentAt IS NULL AND oe.retryCount < :maxRetries ORDER BY oe.createdAt ASC")
    List<OutboxEvent> findUnsentEventsWithRetryLimit(@Param("maxRetries") Integer maxRetries);
    
    @Query("SELECT oe FROM OutboxEvent oe WHERE oe.aggregateType = :aggregateType AND oe.aggregateId = :aggregateId")
    List<OutboxEvent> findByAggregate(@Param("aggregateType") String aggregateType, @Param("aggregateId") Long aggregateId);
    
    @Query("SELECT oe FROM OutboxEvent oe WHERE oe.eventType = :eventType")
    List<OutboxEvent> findByEventType(@Param("eventType") String eventType);
    
    @Query("SELECT oe FROM OutboxEvent oe WHERE oe.createdAt >= :since ORDER BY oe.createdAt DESC")
    List<OutboxEvent> findEventsSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT oe FROM OutboxEvent oe WHERE oe.sentAt IS NOT NULL ORDER BY oe.sentAt DESC")
    List<OutboxEvent> findRecentSentEvents(@Param("limit") Integer limit);
}