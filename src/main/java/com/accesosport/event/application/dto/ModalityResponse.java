package com.accesosport.event.application.dto;

import com.accesosport.event.domain.model.EventModality;

import java.math.BigDecimal;
import java.util.UUID;

public record ModalityResponse(
        UUID id,
        UUID eventId,
        String name,
        BigDecimal distance,
        String distanceUnit,
        BigDecimal price,
        BigDecimal priceWithoutShirt,
        int capacity,
        int registeredCount,
        int availableSpots
) {
    public static ModalityResponse from(EventModality modality) {
        return new ModalityResponse(
                modality.getId(),
                modality.getEventId(),
                modality.getName(),
                modality.getDistance(),
                modality.getDistanceUnit().name(),
                modality.getPrice(),
                modality.getPriceWithoutShirt(),
                modality.getCapacity(),
                modality.getRegisteredCount(),
                modality.getAvailableSpots()
        );
    }
}
