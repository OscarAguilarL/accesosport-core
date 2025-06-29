package com.grupocaos.products.athletix.auth.application.dto;

import com.grupocaos.products.athletix.user.domain.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    private UUID id;
    private String email;
    Set<Role> roles = new HashSet<>();
    private String token;
}
