package com.accesosport.payment.domain.port;

public interface StripeWebhookEventRepository {
    int claimAsProcessing(String eventId, String eventType);
    void markProcessed(String eventId);
    void markFailed(String eventId, String error);
}
