package com.grupocaos.products.athletix.auth.application.dto;

import com.grupocaos.products.athletix.shared.domain.i18n.MessageKeys;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest (
    @NotBlank(message = MessageKeys.AuthMessages.EMAIl_REQUIRED)
    @Email(message = MessageKeys.AuthMessages.INVALID_EMAIL)
    String email,

    @NotBlank(message = MessageKeys.AuthMessages.PASSWORD_REQUIRED)
    String password
) {}
