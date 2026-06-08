package com.accesosport.event.application.dto;

import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.model.EventCapacity;
import com.accesosport.event.domain.model.EventModality;
import com.accesosport.image.application.dto.EventImageResponse;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class EventResponseMapper {

    public static EventResponse toEventResponse(Event event, List<EventModality> modalities, Optional<EventCapacity> eventCapacity) {
        return toEventResponse(event, modalities, eventCapacity, List.of());
    }

    public static EventResponse toEventResponse(Event event, List<EventModality> modalities,
                                                 Optional<EventCapacity> eventCapacity,
                                                 List<EventImageResponse> galleryImages) {
        boolean canRegister = event.getStatus().acceptsRegistrations()
                && event.getRegistrationPeriod().isOpen()
                && eventCapacity.map(EventCapacity::hasAvailability).orElse(false);

        List<ModalityResponse> modalityResponses = modalities.stream()
                .map(ModalityResponse::from)
                .toList();

        int maxCapacity = eventCapacity.map(EventCapacity::getMaxCapacity).orElse(0);
        int reserved = eventCapacity.map(EventCapacity::getReserved).orElse(0);
        int available = eventCapacity.map(EventCapacity::getAvailable).orElse(0);

        return new EventResponse(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getEventDate(),
                mapLocation(event),
                modalityResponses,
                mapRegistrationPeriod(event),
                event.getStatus().name(),
                canRegister,
                mapOrganizer(event),
                event.getCoverImageUrl(),
                galleryImages,
                event.getCreatedOn(),
                event.getWaiverTemplate(),
                maxCapacity,
                reserved,
                available
        );
    }

    public static EventSummaryResponse toEventSummaryResponse(Event event, List<EventModality> modalities, Optional<EventCapacity> eventCapacity) {
        boolean canRegister = event.getStatus().acceptsRegistrations()
                && event.getRegistrationPeriod().isOpen()
                && eventCapacity.map(EventCapacity::hasAvailability).orElse(false);

        BigDecimal minPrice = modalities.stream()
                .map(EventModality::getPrice)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        int totalAvailableSpots = eventCapacity.map(EventCapacity::getAvailable).orElse(0);

        return new EventSummaryResponse(
                event.getId(),
                event.getName(),
                event.getEventDate(),
                event.getLocation().getFullAddress(),
                minPrice,
                totalAvailableSpots,
                event.getStatus().name(),
                canRegister,
                event.getCoverImageUrl()
        );
    }

    public static LocationDto mapLocation(Event event) {
        var location = event.getLocation();
        return new LocationDto(
                location.place(),
                location.city(),
                location.country(),
                location.getFullAddress()
        );
    }

    public static RegistrationPeriodDto mapRegistrationPeriod(Event event) {
        var period = event.getRegistrationPeriod();
        return new RegistrationPeriodDto(period.start(), period.end());
    }

    public static OrganizerDto mapOrganizer(Event event) {
        return new OrganizerDto(
                event.getCreatedBy().getId(),
                event.getCreatedBy().getEmail()
        );
    }
}
