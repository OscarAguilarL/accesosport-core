package com.accesosport.user.infrastructure.persistence.mapper;

import com.accesosport.user.domain.model.Role;
import com.accesosport.user.domain.model.User;
import com.accesosport.user.infrastructure.persistence.entity.RoleJpaEntity;
import com.accesosport.user.infrastructure.persistence.entity.UserJpaEntity;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * A utility class responsible for mapping between User domain model and
 * UserJpaEntity persistence model.
 */
public class UserMapper {

    /**
     * Maps a {@link UserJpaEntity} object to its corresponding {@link User} domain model.
     *
     * @param entity the {@link UserJpaEntity} object to be mapped. Can be null, in which case null will be returned.
     * @return a {@link User} object derived from the given {@link UserJpaEntity}, or null if the input is null.
     */
    public static User toDomain(UserJpaEntity entity) {
        if (entity == null) return null;

        var user = User.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .passwordHash(entity.getPasswordHash())
                .createdAt(entity.getCreatedAt())
                .lastAccess(entity.getLastAccess())
                .roles(mapRolesToDomain(entity.getRoles()));

        if (entity.getAddress() != null) {
            user.address(AddressMapper.mapAddressToDomain(entity.getAddress()));
        }

        if (entity.getPersonalData() != null) {
            user.personalData(PersonalDataMapper.toDomain(entity.getPersonalData()));
        }

        return user.build();
    }

    /**
     * Maps a {@link User} domain model object to its corresponding {@link UserJpaEntity} persistence model.
     *
     * @param domain the {@link User} object to be mapped. Can be null, in which case null will be returned.
     * @return a {@link UserJpaEntity} object derived from the given {@link User}, or null if the input is null.
     */
    public static UserJpaEntity toEntity(User domain) {
        if (domain == null) return null;

        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(domain.getId());
        entity.setEmail(domain.getEmail());
        entity.setPasswordHash(domain.getPasswordHash());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setLastAccess(domain.getLastAccess());
        entity.setRoles(mapRolesToEntity(domain.getRoles()));

        if (domain.getAddress() != null) {
            entity.setAddress(AddressMapper.mapAddressToEntity(domain.getAddress()));
        }

        if (domain.getPersonalData() != null) {
            entity.setPersonalData(PersonalDataMapper.toEntity(domain.getPersonalData()));
        }

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
