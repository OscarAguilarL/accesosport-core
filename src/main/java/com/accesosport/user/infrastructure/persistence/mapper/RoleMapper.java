package com.accesosport.user.infrastructure.persistence.mapper;

import com.accesosport.user.domain.model.Role;
import com.accesosport.user.infrastructure.persistence.entity.RoleJpaEntity;

public class RoleMapper {
    public static Role toDomain(RoleJpaEntity entity) {
        if (entity == null) return null;
        return Role.of(entity.getId(), entity.getRole());
    }

    public static RoleJpaEntity toEntity(Role domain) {
        if (domain == null) return null;
        RoleJpaEntity entity = new RoleJpaEntity();
        entity.setId(domain.getId());
        entity.setRole(domain.getRole());
        return entity;
    }
}
