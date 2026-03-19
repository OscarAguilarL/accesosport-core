package com.accesosport.event.application.dto;

import java.time.LocalDateTime;

public record RegistrationPeriodDto(
        LocalDateTime start,
        LocalDateTime end
) {
}
