package com.accesosport.registration.domain.events;

import com.accesosport.shared.domain.events.DomainEvent;

import java.util.UUID;

/**
 * Se publica cuando un participante cancela su inscripción.
 * MVP-01 añadirá un constructor que acepte la entidad Registration.
 */
public class RegistrationCancelledEvent extends DomainEvent {

    private final UUID registrationId;
    private final UUID eventId;
    private final UUID participantId;

    public RegistrationCancelledEvent(UUID registrationId, UUID eventId, UUID participantId) {
        super("registration.cancelled");
        this.registrationId = registrationId;
        this.eventId = eventId;
        this.participantId = participantId;
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
}
