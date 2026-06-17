package com.accesosport.payment.infrastructure.persistence.jpa;

import com.accesosport.payment.infrastructure.persistence.entity.StripeWebhookEventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface StripeWebhookEventJpaRepository extends JpaRepository<StripeWebhookEventJpaEntity, String> {

    @Modifying
    @Query(value = """
            INSERT INTO stripe_webhook_events (event_id, event_type, status, received_at)
            VALUES (:eventId, :eventType, 'processing', :receivedAt)
            ON CONFLICT (event_id) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(
            @Param("eventId") String eventId,
            @Param("eventType") String eventType,
            @Param("receivedAt") Instant receivedAt
    );

    @Modifying
    @Query(value = """
            UPDATE stripe_webhook_events
            SET status = 'processed', processed_at = :processedAt
            WHERE event_id = :eventId
            """, nativeQuery = true)
    void markProcessed(@Param("eventId") String eventId, @Param("processedAt") Instant processedAt);

    @Modifying
    @Query(value = """
            UPDATE stripe_webhook_events
            SET status = 'failed', last_error = :error, processed_at = :processedAt
            WHERE event_id = :eventId
            """, nativeQuery = true)
    void markFailed(
            @Param("eventId") String eventId,
            @Param("error") String error,
            @Param("processedAt") Instant processedAt
    );
}
