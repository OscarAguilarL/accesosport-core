package com.accesosport.user.infrastructure.persistence.mapper;

import com.accesosport.user.domain.model.UserOrganizerProfile;
import com.accesosport.user.infrastructure.persistence.entity.UserOrganizerProfileJpaEntity;

/**
 * The OrganizerProfileMapper class provides static methods for mapping between
 * UserOrganizerProfile domain objects and UserOrganizerProfileJpaEntity entities.
 * <p>
 * This class is responsible for converting data between the domain and entity layers
 * as part of the application's data flow. It handles null checks and delegates
 * sub-mapping responsibilities to other mappers where needed.
 */
public class OrganizerProfileMapper {

    /**
     * Maps a UserOrganizerProfileJpaEntity entity to a UserOrganizerProfile domain object.
     *
     * @param entity the UserOrganizerProfileJpaEntity to be mapped; can be null.
     * @return the corresponding UserOrganizerProfile domain object, or null if the input entity is null.
     */
    public static UserOrganizerProfile toDomain(UserOrganizerProfileJpaEntity entity) {
        if (entity == null) return null;

        return UserOrganizerProfile.builder()
                .id(entity.getId())
                .organizationName(entity.getOrganizationName())
                .website(entity.getWebsite())
                .facebook(entity.getFacebook())
                .instagram(entity.getInstagram())
                .description(entity.getDescription())
                .verificationStatus(entity.getVerificationStatus())
                .verifiedAt(entity.getVerifiedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .user(UserMapper.toDomain(entity.getUser()))
                .build();
    }

    /**
     * Converts a UserOrganizerProfile domain object into a UserOrganizerProfileJpaEntity entity.
     *
     * @param domain the UserOrganizerProfile domain object to be converted; can be null.
     * @return the corresponding UserOrganizerProfileJpaEntity object, or null if the input domain is null.
     */
    public static UserOrganizerProfileJpaEntity toEntity(UserOrganizerProfile domain) {
        if (domain == null) return null;

        UserOrganizerProfileJpaEntity entity = new UserOrganizerProfileJpaEntity();
        entity.setId(domain.getId());
        entity.setOrganizationName(domain.getOrganizationName());
        entity.setWebsite(domain.getWebsite());
        entity.setFacebook(domain.getFacebook());
        entity.setInstagram(domain.getInstagram());
        entity.setDescription(domain.getDescription());
        entity.setVerificationStatus(domain.getVerificationStatus());
        entity.setVerifiedAt(domain.getVerifiedAt());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setUser(UserMapper.toEntity(domain.getUser()));

        return entity;
    }
}
