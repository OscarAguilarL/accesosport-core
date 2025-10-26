package com.grupocaos.products.athletix.auth.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record RegisterRequest (

    @NotBlank(message = "Email is required")
    @Email(message = "Email is invalid")
    String email,

    @NotBlank(message = "Password is required")
    String password,

    @NotBlank(message = "Password confirmation is required")
    String passwordConfirmation,

    @NotNull(message = "Role is required")
    Set<String> role,

    // Data for runner
    String fullName,
    String address,
    String emergencyContact,
    String emergencyContactRelation,

    // Data for organizer
    String organizationName,
    String organizerPhoneNumber
) {}
