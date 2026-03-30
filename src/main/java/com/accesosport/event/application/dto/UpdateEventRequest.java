package com.accesosport.event.application.dto;

import com.accesosport.event.domain.model.DistanceUnit;
import com.accesosport.event.domain.model.RaceType;
import com.accesosport.shared.domain.i18n.MessageKeys;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UpdateEventRequest(

        @Size(min = 5, max = 200, message = MessageKeys.Events.EVENT_VALIDATION_NAME_LENGTH)
        String name,

        @Size(max = 2000, message = MessageKeys.Events.EVENT_VALIDATION_DESCRIPTION_MAX_LENGTH)
        String description,

        @Future(message = MessageKeys.Events.EVENT_VALIDATION_DATE_FUTURE)
        LocalDateTime eventDate,

        String place,
        String city,
        String country,

        @DecimalMin(value = "-90", message = MessageKeys.Events.EVENT_VALIDATION_LATITUDE_INVALID)
        @DecimalMax(value = "90", message = MessageKeys.Events.EVENT_VALIDATION_LATITUDE_INVALID)
        Double latitude,

        @DecimalMin(value = "-180", message = MessageKeys.Events.EVENT_VALIDATION_LONGITUDE_INVALID)
        @DecimalMax(value = "180", message = MessageKeys.Events.EVENT_VALIDATION_LONGITUDE_INVALID)
        Double longitude,

        RaceType raceType,

        @DecimalMin(value = "0.01", message = MessageKeys.Events.EVENT_VALIDATION_DISTANCE_POSITIVE)
        @DecimalMax(value = "300", message = MessageKeys.Events.EVENT_VALIDATION_DISTANCE_MAX)
        BigDecimal distance,

        DistanceUnit distanceUnit,

        @DecimalMin(value = "0.0", message = MessageKeys.Events.EVENT_VALIDATION_PRICE_POSITIVE)
        BigDecimal price,

        @Future(message = MessageKeys.Events.EVENT_VALIDATION_REGISTRATION_START_FUTURE)
        LocalDateTime registrationStartDate,

        @Future(message = MessageKeys.Events.EVENT_VALIDATION_REGISTRATION_END_FUTURE)
        LocalDateTime registrationEndDate,

        @Min(value = 1, message = MessageKeys.Events.EVENT_VALIDATION_MAX_PARTICIPANTS_POSITIVE)
        Integer maxParticipants

) {
    public UpdateEventRequest {
        if (registrationEndDate != null && registrationStartDate != null) {
            if (registrationEndDate.isBefore(registrationStartDate)) {
                throw new IllegalArgumentException(
                        MessageKeys.Events.EVENT_VALIDATION_REGISTRATION_END_AFTER_START);
            }
        }

        if (eventDate != null && registrationEndDate != null) {
            if (registrationEndDate.isAfter(eventDate)) {
                throw new IllegalArgumentException(
                        MessageKeys.Events.EVENT_VALIDATION_REGISTRATION_BEFORE_EVENT);
            }
        }
    }
}
