package com.grupocaos.products.athletix.event.application.dto;

import com.grupocaos.products.athletix.event.domain.model.DistanceUnit;
import com.grupocaos.products.athletix.event.domain.model.RaceType;
import com.grupocaos.products.athletix.shared.domain.i18n.MessageKeys;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateEventRequest(

        @NotBlank(message = MessageKeys.Events.EVENT_VALIDATION_NAME_REQUIRED)
        @Size(min = 5, max = 200, message = MessageKeys.Events.EVENT_VALIDATION_NAME_LENGTH)
        String name,

        @Size(max = 2000, message = MessageKeys.Events.EVENT_VALIDATION_DESCRIPTION_MAX_LENGTH)
        String description,

        @NotNull(message = MessageKeys.Events.EVENT_VALIDATION_DATE_REQUIRED)
        @Future(message = MessageKeys.Events.EVENT_VALIDATION_DATE_FUTURE)
        LocalDateTime eventDate,

        @NotBlank(message = MessageKeys.Events.EVENT_VALIDATION_LOCATION_REQUIRED)
        String place,
        String city,
        String country,

        @DecimalMin(value = "-90", message = MessageKeys.Events.EVENT_VALIDATION_LATITUDE_INVALID)
        @DecimalMax(value = "90", message = MessageKeys.Events.EVENT_VALIDATION_LATITUDE_INVALID)
        Double latitude,

        @DecimalMin(value = "-180", message = MessageKeys.Events.EVENT_VALIDATION_LONGITUDE_INVALID)
        @DecimalMax(value = "180", message = MessageKeys.Events.EVENT_VALIDATION_LONGITUDE_INVALID)
        Double longitude,

        @NotNull(message = MessageKeys.Events.EVENT_VALIDATION_RACE_TYPE_REQUIRED)
        RaceType raceType,

        @NotNull(message = MessageKeys.Events.EVENT_VALIDATION_DISTANCE_REQUIRED)
        @DecimalMin(value = "0.01", message = MessageKeys.Events.EVENT_VALIDATION_DISTANCE_POSITIVE)
        @DecimalMax(value = "300", message = MessageKeys.Events.EVENT_VALIDATION_DISTANCE_MAX)
        BigDecimal distance,

        @NotNull(message = MessageKeys.Events.EVENT_VALIDATION_DISTANCE_UNIT_REQUIRED)
        DistanceUnit distanceUnit,

        @NotNull(message = MessageKeys.Events.EVENT_VALIDATION_PRICE_REQUIRED)
        @DecimalMin(value = "0.0", message = MessageKeys.Events.EVENT_VALIDATION_PRICE_POSITIVE)
        BigDecimal price,

        @NotNull(message = MessageKeys.Events.EVENT_VALIDATION_REGISTRATION_REQUIRED)
        @Future(message = MessageKeys.Events.EVENT_VALIDATION_REGISTRATION_START_FUTURE)
        LocalDateTime registrationStartDate,

        @NotNull(message = MessageKeys.Events.EVENT_VALIDATION_REGISTRATION_REQUIRED)
        @Future(message = MessageKeys.Events.EVENT_VALIDATION_REGISTRATION_END_FUTURE)
        LocalDateTime registrationEndDate,

        @Min(value = 1, message = MessageKeys.Events.EVENT_VALIDATION_MAX_PARTICIPANTS_POSITIVE)
        Integer maxParticipants

) {
    public CreateEventRequest {
        // (1) registrationEndDate must be after registrationStartDate
        if (registrationEndDate != null && registrationStartDate != null) {
            if (registrationEndDate.isBefore(registrationStartDate)) {
                throw new IllegalArgumentException(MessageKeys.Events.EVENT_VALIDATION_REGISTRATION_END_AFTER_START);
            }
        }

        // (2) Registration must close before event date
        if (eventDate != null && registrationEndDate != null) {
            if (registrationEndDate.isAfter(eventDate)) {
                throw new IllegalArgumentException(MessageKeys.Events.EVENT_VALIDATION_REGISTRATION_BEFORE_EVENT);
            }
        }
    }
}
