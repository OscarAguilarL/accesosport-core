package com.accesosport.user.domain.repository;

import com.accesosport.user.domain.model.Role;
import com.accesosport.user.domain.model.RoleEnumeration;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository {
    Optional<Role> findByRole(RoleEnumeration roleEnumeration);

    boolean existsByRole(RoleEnumeration role);

    Role save(Role role);

    Optional<Role> findById(UUID id);
}
