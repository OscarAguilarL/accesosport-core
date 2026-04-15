package com.accesosport.registration.infrastructure.persistence.adapter;

import com.accesosport.registration.domain.model.Registration;
import com.accesosport.registration.domain.repository.RegistrationRepository;
import com.accesosport.registration.infrastructure.persistence.jpa.RegistrationJpaRepository;
import com.accesosport.registration.infrastructure.persistence.mapper.RegistrationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter class for bridging the domain layer's {@link RegistrationRepository} interface
 * with the data layer's {@link RegistrationJpaRepository}. This class provides concrete
 * implementations by mapping between domain objects and JPA entities via {@link RegistrationMapper}.
 */
@Repository
@RequiredArgsConstructor
public class RegistrationRepositoryAdapter implements RegistrationRepository {

    private final RegistrationJpaRepository jpaRepository;

    @Override
    public Optional<Registration> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(RegistrationMapper::toDomain);
    }

    @Override
    public Optional<Registration> findByTicketCode(String ticketCode) {
        return jpaRepository.findByTicketCode(ticketCode)
                .map(RegistrationMapper::toDomain);
    }

    @Override
    public Registration save(Registration registration) {
        return RegistrationMapper.toDomain(
                jpaRepository.save(RegistrationMapper.toEntity(registration))
        );
    }

    @Override
    public void saveAll(List<Registration> registrations) {
        jpaRepository.saveAll(
                registrations.stream()
                        .map(RegistrationMapper::toEntity)
                        .toList()
        );
    }

    @Override
    public List<Registration> findByEventId(UUID eventId) {
        return jpaRepository.findByEventId(eventId).stream()
                .map(RegistrationMapper::toDomain)
                .toList();
    }

    @Override
    public List<Registration> findConfirmedByEventId(UUID eventId) {
        return jpaRepository.findConfirmedByEventId(eventId).stream()
                .map(RegistrationMapper::toDomain)
                .toList();
    }

    @Override
    public List<Registration> findByParticipantId(UUID participantId) {
        return jpaRepository.findByParticipantId(participantId).stream()
                .map(RegistrationMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByEventIdAndParticipantId(UUID eventId, UUID participantId) {
        return jpaRepository.existsByEventIdAndParticipantId(eventId, participantId);
    }

    @Override
    public List<Registration> findExpiredPendingPayments(LocalDateTime cardThreshold, LocalDateTime cashThreshold) {
        return jpaRepository.findExpiredPendingPayments(cardThreshold, cashThreshold).stream()
                .map(RegistrationMapper::toDomain)
                .toList();
    }
}
