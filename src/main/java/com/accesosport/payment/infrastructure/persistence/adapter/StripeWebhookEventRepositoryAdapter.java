package com.accesosport.payment.infrastructure.persistence.adapter;

import com.accesosport.payment.domain.port.StripeWebhookEventRepository;
import com.accesosport.payment.infrastructure.persistence.jpa.StripeWebhookEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class StripeWebhookEventRepositoryAdapter implements StripeWebhookEventRepository {

    private final StripeWebhookEventJpaRepository jpaRepository;

    @Override
    public int claimAsProcessing(String eventId, String eventType) {
        try {
            return jpaRepository.insertIfAbsent(eventId, eventType, Instant.now());
        } catch (DataIntegrityViolationException e) {
            return 0;
        }
    }

    @Override
    public void markProcessed(String eventId) {
        jpaRepository.markProcessed(eventId, Instant.now());
    }

    @Override
    public void markFailed(String eventId, String error) {
        String truncated = error != null && error.length() > 500 ? error.substring(0, 500) : error;
        jpaRepository.markFailed(eventId, truncated, Instant.now());
    }
}
