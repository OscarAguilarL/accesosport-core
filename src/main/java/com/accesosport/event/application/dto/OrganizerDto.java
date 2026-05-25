package com.accesosport.event.application.dto;

import java.util.UUID;

public record OrganizerDto(
        UUID id,
        String email
) {
}
