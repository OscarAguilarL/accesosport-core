package com.accesosport.registration.infrastructure.persistence.mapper;

import com.accesosport.registration.domain.model.PaymentMethod;
import com.accesosport.registration.domain.model.Registration;
import com.accesosport.registration.domain.model.RegistrationStatus;
import com.accesosport.registration.infrastructure.persistence.entity.RegistrationJpaEntity;

/**
 * Utility class for mapping between {@link Registration} domain objects
 * and {@link RegistrationJpaEntity} JPA entities.
 */
public class RegistrationMapper {

    private RegistrationMapper() {}

    public static Registration toDomain(RegistrationJpaEntity entity) {
        if (entity == null) return null;

        return Registration.reconstitute(
                entity.getId(),
                entity.getEventId(),
                entity.getParticipantId(),
                entity.getModalityId(),
                RegistrationStatus.valueOf(entity.getStatus()),
                entity.getTicketCode(),
                entity.getBibNumber(),
                entity.getPaymentMethod() != null ? PaymentMethod.valueOf(entity.getPaymentMethod()) : null,
                entity.isKitPickedUp(),
                entity.getKitPickedUpAt(),
                entity.getRegisteredAt(),
                entity.getCancelledAt()
        );
    }

    public static RegistrationJpaEntity toEntity(Registration domain) {
        if (domain == null) return null;

        RegistrationJpaEntity entity = new RegistrationJpaEntity();
        entity.setId(domain.getId());
        entity.setEventId(domain.getEventId());
        entity.setParticipantId(domain.getParticipantId());
        entity.setModalityId(domain.getModalityId());
        entity.setStatus(domain.getStatus().name());
        entity.setTicketCode(domain.getTicketCode());
        entity.setBibNumber(domain.getBibNumber());
        entity.setPaymentMethod(domain.getPaymentMethod() != null ? domain.getPaymentMethod().name() : null);
        entity.setKitPickedUp(domain.isKitPickedUp());
        entity.setKitPickedUpAt(domain.getKitPickedUpAt());
        entity.setRegisteredAt(domain.getRegisteredAt());
        entity.setCancelledAt(domain.getCancelledAt());
        return entity;
    }
}
