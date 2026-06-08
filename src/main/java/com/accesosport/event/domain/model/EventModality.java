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
    private int registeredCount;

    private EventModality() {}

    public static EventModality create(UUID eventId, String name, BigDecimal distance, DistanceUnit distanceUnit, BigDecimal price) {
        return create(eventId, name, distance, distanceUnit, price, null);
    }

    public static EventModality create(UUID eventId, String name, BigDecimal distance, DistanceUnit distanceUnit, BigDecimal price, BigDecimal priceWithoutShirt) {
        validate(name, distance, distanceUnit, price);
        EventModality m = new EventModality();
        m.id = UUID.randomUUID();
        m.eventId = eventId;
        m.name = name;
        m.distance = distance;
        m.distanceUnit = distanceUnit;
        m.price = price;
        m.priceWithoutShirt = priceWithoutShirt;
        m.registeredCount = 0;
        return m;
    }

    public static EventModality reconstitute(UUID id, UUID eventId, String name, BigDecimal distance, DistanceUnit distanceUnit, BigDecimal price, BigDecimal priceWithoutShirt, int registeredCount) {
        EventModality m = new EventModality();
        m.id = id;
        m.eventId = eventId;
        m.name = name;
        m.distance = distance;
        m.distanceUnit = distanceUnit;
        m.price = price;
        m.priceWithoutShirt = priceWithoutShirt;
        m.registeredCount = registeredCount;
        return m;
    }

    public boolean hasRegistrations() {
        return registeredCount > 0;
    }

    private static void validate(String name, BigDecimal distance, DistanceUnit distanceUnit, BigDecimal price) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Modality name is required");
        if (distance == null || distance.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Distance must be positive");
        if (distanceUnit == null) throw new IllegalArgumentException("Distance unit is required");
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Price must be non-negative");
    }
}
