package com.grupocaos.products.athletix.user.infrastructure.persistence.mapper;

import com.grupocaos.products.athletix.user.domain.model.PersonalData;
import com.grupocaos.products.athletix.user.infrastructure.persistence.entity.PersonalDataEmbeddable;

/**
 * Utility class for mapping between {@link PersonalDataEmbeddable} entities and {@link PersonalData} domain objects.
 * Provides methods to convert data in both directions, ensuring consistent representation of personal data
 * across the application layers.
 */
public class PersonalDataMapper {

    /**
     * Converts a {@link PersonalDataEmbeddable} entity to a {@link PersonalData} domain object.
     * Populates the domain object with the corresponding field values from the entity.
     *
     * @param entity the {@link PersonalDataEmbeddable} entity to be converted. Must not be null.
     * @return a {@link PersonalData} object populated with the corresponding values from the {@link PersonalDataEmbeddable} entity.
     */
    public static PersonalData toDomain(PersonalDataEmbeddable entity) {
        PersonalData personalData = new PersonalData();
        personalData.setFirstName(entity.getFirstName());
        personalData.setLastName(entity.getLastName());
        personalData.setSecondLastName(entity.getSecondLastName());
        personalData.setBirthDate(entity.getBirthDate());
        personalData.setGender(entity.getGender());
        personalData.setPhoneNumber(entity.getPhoneNumber());
        return personalData;
    }

    /**
     * Maps a {@link PersonalData} domain object to a {@link PersonalDataEmbeddable} entity.
     * Transfers all personal data fields from the domain model to the embeddable entity.
     *
     * @param domain the {@link PersonalData} object to be converted. Must not be null.
     * @return a {@link PersonalDataEmbeddable} entity populated with the corresponding values from the {@link PersonalData} domain object.
     */
    public static PersonalDataEmbeddable toEntity(PersonalData domain) {
        PersonalDataEmbeddable entity = new PersonalDataEmbeddable();
        entity.setFirstName(domain.getFirstName());
        entity.setLastName(domain.getLastName());
        entity.setSecondLastName(domain.getSecondLastName());
        entity.setBirthDate(domain.getBirthDate());
        entity.setGender(domain.getGender());
        entity.setPhoneNumber(domain.getPhoneNumber());
        return entity;
    }
}
