package com.accesosport.event.domain.model;

import com.accesosport.event.domain.exception.NoCapacityException;

import java.util.UUID;

/**
 * Aggregate responsible for managing event registration capacity.
 * Separated from Event so that atomic capacity operations (reserve/release)
 * can be isolated and eventually extracted to a dedicated microservice or Redis backend.
 *
 * <p>maxCapacity null means unlimited capacity.</p>
 */
public class EventCapacity {

    private final UUID eventId;
    private int reserved;
    private Integer maxCapacity; // null = unlimited

    private EventCapacity(UUID eventId, int reserved, Integer maxCapacity) {
        this.eventId = eventId;
        this.reserved = reserved;
        this.maxCapacity = maxCapacity;
    }

    public static EventCapacity create(UUID eventId, Integer maxCapacity) {
        return new EventCapacity(eventId, 0, maxCapacity);
    }

    public static EventCapacity reconstitute(UUID eventId, int reserved, Integer maxCapacity) {
        return new EventCapacity(eventId, reserved, maxCapacity);
    }

    public boolean hasAvailability() {
        return maxCapacity == null || reserved < maxCapacity;
    }

    public int getAvailable() {
        if (maxCapacity == null) return Integer.MAX_VALUE;
        return Math.max(0, maxCapacity - reserved);
    }

    /**
     * Non-concurrent reserve for admin or test usage.
     * In production, use {@code EventCapacityJpaRepository#reserveIfAvailable} for atomic reservation.
     */
    public void reserve() {
        if (!hasAvailability()) throw new NoCapacityException(eventId);
        this.reserved++;
    }

    public void release() {
        if (reserved > 0) this.reserved--;
    }

    public void updateMaxCapacity(Integer newMaxCapacity) {
        if (newMaxCapacity != null && newMaxCapacity <= 0) {
            throw new IllegalArgumentException("maxCapacity must be positive");
        }
        this.maxCapacity = newMaxCapacity;
    }

    public UUID getEventId() { return eventId; }
    public int getReserved() { return reserved; }
    public Integer getMaxCapacity() { return maxCapacity; }
}
