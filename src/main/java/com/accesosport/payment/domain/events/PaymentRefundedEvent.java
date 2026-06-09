package com.accesosport.payment.domain.events;

import com.accesosport.shared.domain.events.DomainEvent;

import java.util.UUID;

public class PaymentRefundedEvent extends DomainEvent {

    private final UUID paymentId;
    private final UUID registrationId;

    public PaymentRefundedEvent(UUID paymentId, UUID registrationId) {
        super("payment.refunded");
        this.paymentId = paymentId;
        this.registrationId = registrationId;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public UUID getRegistrationId() {
        return registrationId;
    }
}
