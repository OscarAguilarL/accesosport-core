package com.accesosport.event.domain.model;

import lombok.Getter;

import java.util.UUID;

@Getter
public class EventCapacity {

    private UUID eventId;
    private int reserved;
    private int maxCapacity;

    private EventCapacity() {}

    public static EventCapacity create(UUID eventId, int maxCapacity) {
        if (maxCapacity <= 0) throw new IllegalArgumentException("maxCapacity must be positive");
        EventCapacity ec = new EventCapacity();
        ec.eventId = eventId;
        ec.reserved = 0;
        ec.maxCapacity = maxCapacity;
        return ec;
    }

    public static EventCapacity reconstitute(UUID eventId, int reserved, int maxCapacity) {
        EventCapacity ec = new EventCapacity();
        ec.eventId = eventId;
        ec.reserved = reserved;
        ec.maxCapacity = maxCapacity;
        return ec;
    }

    public boolean hasAvailability() {
        return reserved < maxCapacity;
    }

    public int getAvailable() {
        return maxCapacity - reserved;
    }

    public void release() {
        if (reserved > 0) reserved--;
    }
}
