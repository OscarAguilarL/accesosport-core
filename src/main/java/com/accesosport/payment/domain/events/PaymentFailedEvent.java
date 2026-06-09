package com.accesosport.payment.domain.events;

import com.accesosport.shared.domain.events.DomainEvent;

import java.util.UUID;

public class PaymentFailedEvent extends DomainEvent {

    private final UUID registrationId;

    public PaymentFailedEvent(UUID registrationId) {
        super("payment.failed");
        this.registrationId = registrationId;
    }

    public UUID getRegistrationId() {
        return registrationId;
    }
}
