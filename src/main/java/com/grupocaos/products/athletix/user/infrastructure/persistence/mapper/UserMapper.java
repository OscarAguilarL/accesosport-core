package com.grupocaos.products.athletix.user.infrastructure.persistence.mapper;

import com.grupocaos.products.athletix.user.domain.model.Role;
import com.grupocaos.products.athletix.user.domain.model.User;
import com.grupocaos.products.athletix.user.infrastructure.persistence.entity.RoleJpaEntity;
import com.grupocaos.products.athletix.user.infrastructure.persistence.entity.UserJpaEntity;

import java.util.Set;
import java.util.stream.Collectors;

public class UserMapper {

    public static User toDomain(UserJpaEntity entity) {
        if (entity == null) return null;

        return User.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .passwordHash(entity.getPasswordHash())
                .createdAt(entity.getCreatedAt())
                .lastAccess(entity.getLastAccess())
                .roles(mapRolesToDomain(entity.getRoles()))
                .build();
    }

    public static UserJpaEntity toEntity(User domain) {
        if (domain == null) return null;

        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(domain.getId());
        entity.setEmail(domain.getEmail());
        entity.setPasswordHash(domain.getPasswordHash());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setLastAccess(domain.getLastAccess());
        entity.setRoles(mapRolesToEntity(domain.getRoles()));
        return entity;
    }

    private static Set<Role> mapRolesToDomain(Set<RoleJpaEntity> entities) {
        if (entities == null) return Set.of();
        return entities.stream()
                .map(RoleMapper::toDomain)
                .collect(Collectors.toSet());
    }

    private static Set<RoleJpaEntity> mapRolesToEntity(Set<Role> roles) {
        if (roles == null) return Set.of();
        return roles.stream()
                .map(RoleMapper::toEntity)
                .collect(Collectors.toSet());
    }
}
