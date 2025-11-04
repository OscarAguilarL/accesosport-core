package com.grupocaos.products.athletix.event.application.dto;

import com.grupocaos.products.athletix.event.domain.model.DistanceUnit;
import com.grupocaos.products.athletix.event.domain.model.RaceType;
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
        @NotBlank(message = "The event name is required")
        @Size(min = 5, max = 200, message = "The name must be between 5 and 200 characters")
        String name,

        @Size(max = 2000, message = "The description must be less than 2000 characters")
        String description,

        @NotNull(message = "Event date is required")
        @Future(message = "Event date must be a future date")
        LocalDateTime eventDate,

        @NotBlank(message = "The place is required")
        String place,
        String city,
        String country,

        @DecimalMin(value = "-90", message = "Invalid latitude")
        @DecimalMax(value = "90", message = "Invalid latitude")
        Double latitude,

        @DecimalMin(value = "-180", message = "Invalid longitude")
        @DecimalMax(value = "180", message = "Invalid longitude")
        Double longitude,

        @NotNull(message = "The race type is required")
        RaceType raceType,

        @NotNull(message = "The distance is required")
        @DecimalMin(value = "0.01", message = "The distance must be greater than zero")
        @DecimalMax(value = "300", message = "The distance must be less than 300 km")
        BigDecimal distance,

        @NotNull(message = "The distance unit is required")
        DistanceUnit distanceUnit,

        @NotNull(message = "The registration fee (price) is required")
        @DecimalMin(value = "0.0", message = "The registration fee must be greater than zero")
        BigDecimal price,

        @NotNull(message = "The registration start date is required")
        @Future(message = "Registration start date must be a future date")
        LocalDateTime registrationStartDate,

        @NotNull(message = "The registration end date is required")
        @Future(message = "Registration end date must be a future date")
        LocalDateTime registrationEndDate,

        @Min(value = 1, message = "The maximum number of participants must be at least 1")
        Integer maxParticipants
) {
    public CreateEventRequest {
        if (registrationEndDate != null && registrationStartDate != null) {
            if (registrationEndDate.isBefore(registrationStartDate)) {
                throw new IllegalArgumentException("Registration end date must be after registration start date");
            }
        }

        if (eventDate != null && registrationEndDate != null) {
            if (registrationEndDate.isAfter(eventDate)) {
                throw new IllegalArgumentException("Registration must be closed before event date");
            }
        }
    }
}
