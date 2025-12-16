package com.grupocaos.products.athletix.auth.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

import com.grupocaos.products.athletix.shared.domain.i18n.MessageKeys;

public record RegisterRequest (

    @NotBlank(message = MessageKeys.AuthMessages.EMAIl_REQUIRED)
    @Email(message = MessageKeys.AuthMessages.INVALID_EMAIL)
    String email,

    @NotBlank(message = MessageKeys.AuthMessages.PASSWORD_REQUIRED)
    String password,

    @NotBlank(message = MessageKeys.AuthMessages.PASSWORD_CONFIRMATION_REQUIRED)
    String passwordConfirmation,

    // Data for runner
    String fullName,
    String address,
    String emergencyContact,
    String emergencyContactRelation,

    // Data for organizer
    String organizationName,
    String organizerPhoneNumber
) {}
