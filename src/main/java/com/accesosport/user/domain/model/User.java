package com.accesosport.user.domain.model;

import com.accesosport.shared.domain.valueobjects.Address;
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
    private PersonalData personalData;
    private Address address;
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
}
