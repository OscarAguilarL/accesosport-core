package com.accesosport.payment.domain.events;

import com.accesosport.shared.domain.events.DomainEvent;

import java.util.UUID;

public class PaymentManualRefundRequiredEvent extends DomainEvent {

    private final UUID paymentId;
    private final UUID registrationId;

    public PaymentManualRefundRequiredEvent(UUID paymentId, UUID registrationId) {
        super("payment.manual-refund-required");
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
