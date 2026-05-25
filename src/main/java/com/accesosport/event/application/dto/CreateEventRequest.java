package com.accesosport.event.application.dto;

import com.accesosport.shared.domain.i18n.MessageKeys;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

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

        @NotNull(message = MessageKeys.Events.EVENT_VALIDATION_REGISTRATION_REQUIRED)
        @Future(message = MessageKeys.Events.EVENT_VALIDATION_REGISTRATION_START_FUTURE)
        LocalDateTime registrationStartDate,

        @NotNull(message = MessageKeys.Events.EVENT_VALIDATION_REGISTRATION_REQUIRED)
        @Future(message = MessageKeys.Events.EVENT_VALIDATION_REGISTRATION_END_FUTURE)
        LocalDateTime registrationEndDate,

        @NotNull
        @Size(min = 1, message = "El evento debe tener al menos una modalidad")
        @Valid
        List<CreateModalityRequest> modalities

) {
    public CreateEventRequest {
        if (registrationEndDate != null && registrationStartDate != null) {
            if (registrationEndDate.isBefore(registrationStartDate)) {
                throw new IllegalArgumentException(MessageKeys.Events.EVENT_VALIDATION_REGISTRATION_END_AFTER_START);
            }
        }
        if (eventDate != null && registrationEndDate != null) {
            if (registrationEndDate.isAfter(eventDate)) {
                throw new IllegalArgumentException(MessageKeys.Events.EVENT_VALIDATION_REGISTRATION_BEFORE_EVENT);
            }
        }
    }
}
