package com.accesosport.event.application.dto;

import com.accesosport.event.domain.model.DistanceUnit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateModalityRequest(
        @NotBlank String name,
        @NotNull @Positive BigDecimal distance,
        @NotNull DistanceUnit distanceUnit,
        @NotNull @DecimalMin("0") BigDecimal price,
        BigDecimal priceWithoutShirt,
        @NotNull @Positive Integer capacity
) {}
