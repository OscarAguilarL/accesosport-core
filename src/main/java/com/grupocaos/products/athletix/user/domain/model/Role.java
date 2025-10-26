package com.grupocaos.products.athletix.user.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    private UUID id;
    private RoleEnumeration role;

    public static Role of(RoleEnumeration roleEnumeration) {
        Role role = new Role();
        role.role = roleEnumeration;
        return role;
    }

    public static Role of(UUID id, RoleEnumeration roleEnumeration) {
        Role role = new Role();
        role.id = id;
        role.role = roleEnumeration;
        return role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role1 = (Role) o;
        return role == role1.role;
    }

    @Override
    public int hashCode() {
        return role != null ? role.hashCode() : 0;
    }
}
