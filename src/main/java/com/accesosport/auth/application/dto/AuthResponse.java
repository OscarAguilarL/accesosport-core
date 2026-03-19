package com.accesosport.auth.application.dto;

import java.util.Set;
import java.util.UUID;

public record AuthResponse(
        UUID id,
        String email,
        Set<String> roles,
        String token
) {
}
