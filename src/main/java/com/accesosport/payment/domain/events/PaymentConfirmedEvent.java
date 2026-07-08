package com.accesosport.payment.domain.events;

import com.accesosport.shared.domain.events.DomainEvent;

import java.util.UUID;

public class PaymentConfirmedEvent extends DomainEvent {

    private final UUID paymentId;
    private final UUID registrationId;

    public PaymentConfirmedEvent(UUID paymentId, UUID registrationId) {
        super("payment.confirmed");
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
