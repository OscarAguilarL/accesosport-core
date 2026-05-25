package com.accesosport.event.domain.model;

import lombok.Getter;

import java.util.UUID;

@Getter
public class EventCategory {

    private UUID id;
    private UUID eventId;
    private UUID modalityId;
    private String name;
    private Integer minAge;
    private Integer maxAge;

    private EventCategory() {}

    public static EventCategory create(UUID eventId, UUID modalityId, String name, Integer minAge, Integer maxAge) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Category name is required");
        EventCategory c = new EventCategory();
        c.id = UUID.randomUUID();
        c.eventId = eventId;
        c.modalityId = modalityId;
        c.name = name;
        c.minAge = minAge;
        c.maxAge = maxAge;
        return c;
    }

    public static EventCategory reconstitute(UUID id, UUID eventId, UUID modalityId, String name, Integer minAge, Integer maxAge) {
        EventCategory c = new EventCategory();
        c.id = id;
        c.eventId = eventId;
        c.modalityId = modalityId;
        c.name = name;
        c.minAge = minAge;
        c.maxAge = maxAge;
        return c;
    }
}
