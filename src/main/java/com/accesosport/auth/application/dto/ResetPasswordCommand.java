package com.accesosport.auth.application.dto;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordCommand(
        @NotBlank String token,
        @NotBlank String newPassword
) {
}
