package com.accesosport.user.infrastructure.persistence.jpa;

import com.accesosport.user.domain.model.RoleEnumeration;
import com.accesosport.user.infrastructure.persistence.entity.RoleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleJpaRepository extends JpaRepository<RoleJpaEntity, UUID> {
    Optional<RoleJpaEntity> findByRole(RoleEnumeration role);

    boolean existsByRole(RoleEnumeration role);
}
