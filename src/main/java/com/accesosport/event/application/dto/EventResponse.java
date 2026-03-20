package com.accesosport.event.application.dto;

import com.accesosport.image.application.dto.EventImageResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EventResponse(
        UUID id,
        String name,
        String description,
        LocalDateTime eventDate,
        LocationDto location,
        String raceType,
        String Distance,
        BigDecimal price,
        RegistrationPeriodDto registrationPeriod,
        Integer maxParticipants,
        Integer registeredParticipants,
        Integer registrationsAvailable,
        String status,
        boolean canRegister,
        OrganizerDto organizer,
        String coverImageUrl,
        List<EventImageResponse> galleryImages,
        LocalDateTime createdAt
) {
}
