package com.accesosport.event.domain.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class EventModality {

    private UUID id;
    private UUID eventId;
    private String name;
    private BigDecimal distance;
    private DistanceUnit distanceUnit;
    private BigDecimal price;
    private BigDecimal priceWithoutShirt;
    private int capacity;
    private int registeredCount;

    private EventModality() {}

    public static EventModality create(UUID eventId, String name, BigDecimal distance, DistanceUnit distanceUnit, BigDecimal price, int capacity) {
        return create(eventId, name, distance, distanceUnit, price, null, capacity);
    }

    public static EventModality create(UUID eventId, String name, BigDecimal distance, DistanceUnit distanceUnit, BigDecimal price, BigDecimal priceWithoutShirt, int capacity) {
        validate(name, distance, distanceUnit, price, capacity);
        EventModality m = new EventModality();
        m.id = UUID.randomUUID();
        m.eventId = eventId;
        m.name = name;
        m.distance = distance;
        m.distanceUnit = distanceUnit;
        m.price = price;
        m.priceWithoutShirt = priceWithoutShirt;
        m.capacity = capacity;
        m.registeredCount = 0;
        return m;
    }

    public static EventModality reconstitute(UUID id, UUID eventId, String name, BigDecimal distance, DistanceUnit distanceUnit, BigDecimal price, BigDecimal priceWithoutShirt, int capacity, int registeredCount) {
        EventModality m = new EventModality();
        m.id = id;
        m.eventId = eventId;
        m.name = name;
        m.distance = distance;
        m.distanceUnit = distanceUnit;
        m.price = price;
        m.priceWithoutShirt = priceWithoutShirt;
        m.capacity = capacity;
        m.registeredCount = registeredCount;
        return m;
    }

    public boolean hasRegistrations() {
        return registeredCount > 0;
    }

    public int getAvailableSpots() {
        return Math.max(0, capacity - registeredCount);
    }

    private static void validate(String name, BigDecimal distance, DistanceUnit distanceUnit, BigDecimal price, int capacity) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Modality name is required");
        if (distance == null || distance.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Distance must be positive");
        if (distanceUnit == null) throw new IllegalArgumentException("Distance unit is required");
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Price must be non-negative");
        if (capacity <= 0) throw new IllegalArgumentException("Capacity must be positive");
    }
}
