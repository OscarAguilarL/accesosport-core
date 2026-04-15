package com.accesosport.registration.domain.events;

import com.accesosport.registration.domain.model.PaymentMethod;
import com.accesosport.shared.domain.events.DomainEvent;

import java.util.UUID;

/**
 * Se publica cuando una inscripción en estado PENDING_PAYMENT expira sin completar el pago.
 * Tarjeta: 30 minutos. OXXO/efectivo: 3 días.
 * MVP-05 usará este evento para enviar un email de aviso al participante.
 */
public class PendingPaymentExpiredEvent extends DomainEvent {

    private final UUID registrationId;
    private final UUID eventId;
    private final UUID participantId;
    private final PaymentMethod paymentMethod;

    public PendingPaymentExpiredEvent(
            UUID registrationId,
            UUID eventId,
            UUID participantId,
            PaymentMethod paymentMethod
    ) {
        super("registration.pending_payment_expired");
        this.registrationId = registrationId;
        this.eventId = eventId;
        this.participantId = participantId;
        this.paymentMethod = paymentMethod;
    }

    public UUID getRegistrationId() {
        return registrationId;
    }

    public UUID getEventId() {
        return eventId;
    }

    public UUID getParticipantId() {
        return participantId;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
}
