package com.accesosport.auth.application.dto;

import com.accesosport.user.domain.model.User;

import java.util.stream.Collectors;

public class AuthResponseMapper {

    public static AuthResponse fromDomain(User user, String token) {
        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getRoles().stream()
                        .map(role -> role.getRole().name())
                        .collect(Collectors.toSet()),
                token
        );
    }
}
