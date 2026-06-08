package com.accesosport.invitation.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateInvitationRequest(
        @NotBlank
        @Email
        String email,

        @Size(max = 500)
        String reason
) {}
