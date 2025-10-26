package com.grupocaos.products.athletix.user.infrastructure.persistence.jpa;

import com.grupocaos.products.athletix.user.domain.model.RoleEnumeration;
import com.grupocaos.products.athletix.user.infrastructure.persistence.entity.RoleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleJpaRepository extends JpaRepository<RoleJpaEntity, UUID> {
    Optional<RoleJpaEntity> findByRole(RoleEnumeration role);

    boolean existsByRole(RoleEnumeration role);
}
