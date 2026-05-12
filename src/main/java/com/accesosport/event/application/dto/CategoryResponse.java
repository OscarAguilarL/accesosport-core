package com.accesosport.event.application.dto;

import com.accesosport.event.domain.model.EventCategory;

import java.util.UUID;

public record CategoryResponse(
        UUID id,
        UUID eventId,
        UUID modalityId,
        String name,
        Integer minAge,
        Integer maxAge
) {
    public static CategoryResponse from(EventCategory category) {
        return new CategoryResponse(
                category.getId(),
                category.getEventId(),
                category.getModalityId(),
                category.getName(),
                category.getMinAge(),
                category.getMaxAge()
        );
    }
}
