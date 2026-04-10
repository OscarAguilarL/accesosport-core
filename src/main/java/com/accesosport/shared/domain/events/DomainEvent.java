package com.accesosport.shared.domain.events;

import java.time.Instant;
import java.util.UUID;

public abstract class DomainEvent {

    private final UUID eventId = UUID.randomUUID();
    private final Instant occurredAt = Instant.now();
    private final String eventType;

    protected DomainEvent(String eventType) {
        this.eventType = eventType;
    }

    public UUID getEventId() {
        return eventId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getEventType() {
        return eventType;
    }
}
