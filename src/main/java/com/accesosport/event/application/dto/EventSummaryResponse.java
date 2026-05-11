package com.accesosport.event.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record EventSummaryResponse(
        UUID id,
        String name,
        LocalDateTime eventDate,
        String location,
        BigDecimal minPrice,
        int totalAvailableSpots,
        String status,
        boolean canRegister
) {
}
