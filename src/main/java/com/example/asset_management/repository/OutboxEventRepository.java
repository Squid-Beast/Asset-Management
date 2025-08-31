package com.example.asset_management.repository;

import com.example.asset_management.model.OutboxEvent;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OutboxEventRepository extends CrudRepository<OutboxEvent, Long> {
    
    @Query("SELECT * FROM outbox_events WHERE sent_at IS NULL ORDER BY created_at ASC")
    List<OutboxEvent> findUnsentEvents();
    
    @Query("SELECT * FROM outbox_events WHERE sent_at IS NULL AND retry_count < :maxRetries ORDER BY created_at ASC")
    List<OutboxEvent> findUnsentEventsWithRetryLimit(@Param("maxRetries") Integer maxRetries);
    
    @Query("SELECT * FROM outbox_events WHERE aggregate_type = :aggregateType AND aggregate_id = :aggregateId")
    List<OutboxEvent> findByAggregate(@Param("aggregateType") String aggregateType, @Param("aggregateId") Long aggregateId);
    
    @Query("SELECT * FROM outbox_events WHERE event_type = :eventType")
    List<OutboxEvent> findByEventType(@Param("eventType") String eventType);
    
    @Query("SELECT * FROM outbox_events WHERE created_at >= :since ORDER BY created_at DESC")
    List<OutboxEvent> findEventsSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT * FROM outbox_events WHERE sent_at IS NOT NULL ORDER BY sent_at DESC LIMIT :limit")
    List<OutboxEvent> findRecentSentEvents(@Param("limit") Integer limit);
}
