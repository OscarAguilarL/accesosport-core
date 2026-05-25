package com.accesosport.user.infrastructure.persistence.mapper;

import com.accesosport.user.domain.model.UserParticipantProfile;
import com.accesosport.user.infrastructure.persistence.entity.UserParticipantProfileJpaEntity;

/**
 * This class is responsible for mapping between
 * {@link UserParticipantProfileJpaEntity} and {@link UserParticipantProfile}.
 * It provides methods for converting JPA entities to domain objects and vice versa.
 */
public class ParticipantProfileMapper {

    /**
     * Maps a {@link UserParticipantProfileJpaEntity} to a {@link UserParticipantProfile} domain object.
     *
     * @param entity the {@link UserParticipantProfileJpaEntity} to be mapped; can be null
     * @return the corresponding {@link UserParticipantProfile} domain object, or null if the provided entity is null
     */
    public static UserParticipantProfile toDomain(UserParticipantProfileJpaEntity entity) {
        if (entity == null) return null;

        return UserParticipantProfile.builder()
                .id(entity.getId())
                .shirtSize(entity.getShirtSize())
                .emergencyContactName(entity.getEmergencyContactName())
                .emergencyContactPhone(entity.getEmergencyContactPhone())
                .medicalConditions(entity.getMedicalConditions())
                .bloodType(entity.getBloodType())
                .phone(entity.getPhone())
                .gender(entity.getGender())
                .user(UserMapper.toDomain(entity.getUser()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Maps a {@link UserParticipantProfile} domain object to a {@link UserParticipantProfileJpaEntity}.
     *
     * @param domain the {@link UserParticipantProfile} domain object to be mapped; can be null
     * @return the corresponding {@link UserParticipantProfileJpaEntity} object, or null if the provided domain is null
     */
    public static UserParticipantProfileJpaEntity toEntity(UserParticipantProfile domain) {
        if (domain == null) return null;

        UserParticipantProfileJpaEntity entity = new UserParticipantProfileJpaEntity();
        entity.setId(domain.getId());
        entity.setShirtSize(domain.getShirtSize());
        entity.setEmergencyContactName(domain.getEmergencyContactName());
        entity.setEmergencyContactPhone(domain.getEmergencyContactPhone());
        entity.setMedicalConditions(domain.getMedicalConditions());
        entity.setBloodType(domain.getBloodType());
        entity.setPhone(domain.getPhone());
        entity.setGender(domain.getGender());
        entity.setUser(UserMapper.toEntity(domain.getUser()));
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());

        return entity;
    }
}
