package com.accesosport.user.infrastructure.persistence.adapter;

import com.accesosport.user.domain.model.Role;
import com.accesosport.user.domain.model.RoleEnumeration;
import com.accesosport.user.domain.repository.RoleRepository;
import com.accesosport.user.infrastructure.persistence.jpa.RoleJpaRepository;
import com.accesosport.user.infrastructure.persistence.mapper.RoleMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@AllArgsConstructor
public class RoleRepositoryAdapter implements RoleRepository {
    private final RoleJpaRepository roleJpaRepository;

    @Override
    public Optional<Role> findByRole(RoleEnumeration roleEnumeration) {
        return roleJpaRepository.findByRole(roleEnumeration)
                .map(RoleMapper::toDomain);
    }

    @Override
    public boolean existsByRole(RoleEnumeration role) {
        return roleJpaRepository.existsByRole(role);
    }

    @Override
    public Role save(Role role) {
        var entity = RoleMapper.toEntity(role);
        var savedEntity = roleJpaRepository.save(entity);
        return RoleMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Role> findById(UUID id) {
        return roleJpaRepository.findById(id)
                .map(RoleMapper::toDomain);
    }
}
