package com.grupocaos.products.athletix.event.application.dto;

import com.grupocaos.products.athletix.event.domain.model.Event;

public class EventResponseMapper {

    public static EventResponse toEventResponse(Event event) {
        return new EventResponse(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getEventDate(),
                mapLocation(event),
                event.getRaceType().getName(),
                event.getDistance().toString(),
                event.getPrice(),
                mapRegistrationPeriod(event),
                event.getMaxParticipants(),
                event.getRegisteredParticipants(),
                event.getAvailableParticipants(),
                event.getStatus().name(),
                event.canRegister(),
                mapOrganizer(event),
                event.getCreatedOn()
        );
    }

    public static EventSummaryResponse toEventSummaryResponse(Event event) {
        return new EventSummaryResponse(
                event.getId(),
                event.getName(),
                event.getEventDate(),
                event.getLocation().getFullAddress(),
                event.getDistance().toString(),
                event.getPrice(),
                event.getAvailableParticipants(),
                event.getStatus().name(),
                event.canRegister()
        );
    }

    public static LocationDto mapLocation(Event event) {
        var location = event.getLocation();
        return new LocationDto(
                location.place(),
                location.city(),
                location.country(),
                location.latitude(),
                location.longitude(),
                location.getFullAddress()
        );
    }

    public static RegistrationPeriodDto mapRegistrationPeriod(Event event) {
        var period = event.getRegistrationPeriod();
        return new RegistrationPeriodDto(
                period.start(),
                period.end(),
                period.isOpen(),
                period.hasAlreadyClosed()
        );
    }

    public static OrganizerDto mapOrganizer(Event event) {
        return new OrganizerDto(
                event.getCreatedBy().getId(),
                event.getCreatedBy().getEmail()
        );
    }
}
