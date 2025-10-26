package com.grupocaos.products.athletix.user.domain.repository;

import com.grupocaos.products.athletix.user.domain.model.Role;
import com.grupocaos.products.athletix.user.domain.model.RoleEnumeration;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository {
    Optional<Role> findByRole(RoleEnumeration roleEnumeration);

    boolean existsByRole(RoleEnumeration role);

    Role save(Role role);

    Optional<Role> findById(UUID id);
}
