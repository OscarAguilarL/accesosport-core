package com.grupocaos.products.athletix.auth.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email is invalid")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Password confirmation is required")
    private String passwordConfirmation;

    @NotNull(message = "Role is required")
    private Set<String> role;

    // Data for runner
    private String fullName;
    private String address;
    private String emergencyContact;
    private String emergencyContactRelation;

    // Data for organizer
    private String organizationName;
    private String organizerPhoneNumber;
}
