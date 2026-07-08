package com.accesosport.registration.domain.events;

import com.accesosport.shared.domain.events.DomainEvent;

import java.util.UUID;

public class RegistrationCancelledEvent extends DomainEvent {

    private final UUID registrationId;
    private final UUID eventId;
    private final UUID participantId;
    private final int daysUntilEvent;

    public RegistrationCancelledEvent(UUID registrationId, UUID eventId, UUID participantId, int daysUntilEvent) {
        super("registration.cancelled");
        this.registrationId = registrationId;
        this.eventId = eventId;
        this.participantId = participantId;
        this.daysUntilEvent = daysUntilEvent;
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

    public int getDaysUntilEvent() {
        return daysUntilEvent;
    }
}
