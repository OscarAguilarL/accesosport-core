package com.accesosport.event.domain.events;

import com.accesosport.event.domain.model.Event;
import com.accesosport.shared.domain.events.DomainEvent;

import java.util.List;
import java.util.UUID;

public class EventCancelledEvent extends DomainEvent {

    private final UUID eventId;
    private final String eventName;
    private final String cancellationReason;
    private final List<UUID> affectedRegistrationIds;

    public EventCancelledEvent(Event event, String cancellationReason, List<UUID> affectedRegistrationIds) {
        super("event.cancelled");
        this.eventId = event.getId();
        this.eventName = event.getName();
        this.cancellationReason = cancellationReason;
        this.affectedRegistrationIds = List.copyOf(affectedRegistrationIds);
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public List<UUID> getAffectedRegistrationIds() {
        return affectedRegistrationIds;
    }
}
