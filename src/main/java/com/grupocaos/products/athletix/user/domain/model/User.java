package com.grupocaos.products.athletix.user.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private UUID id;
    private String email;
    private String passwordHash;
    private LocalDateTime createdAt;
    private LocalDateTime lastAccess;
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    public void updateLastAccess() {
        this.lastAccess = LocalDateTime.now();
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public boolean hasRole(RoleEnumeration roleEnumeration) {
        return roles.stream()
                .anyMatch(role -> role.getRole().equals(roleEnumeration));
    }

    public void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
    }
}
