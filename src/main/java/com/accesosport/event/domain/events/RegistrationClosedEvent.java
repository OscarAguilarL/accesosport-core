package com.accesosport.event.domain.events;

import com.accesosport.shared.domain.events.DomainEvent;
import java.util.UUID;

public class RegistrationClosedEvent extends DomainEvent {

    private final UUID eventId;

    public RegistrationClosedEvent(UUID eventId) {
        super("event.registration_closed");
        this.eventId = eventId;
    }

    public UUID getEventId() {
        return eventId;
    }
}
