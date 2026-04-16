package com.accesosport.registration.infrastructure.persistence.jpa;

import com.accesosport.registration.infrastructure.persistence.entity.RegistrationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for performing CRUD and custom operations on RegistrationJpaEntity.
 * Extends JpaRepository to provide standard data access methods for RegistrationJpaEntity.
 */
public interface RegistrationJpaRepository extends JpaRepository<RegistrationJpaEntity, UUID> {

    /**
     * Finds a registration by its unique ticket code.
     *
     * @param ticketCode the ticket code to search for
     * @return the registration entity if found, otherwise empty
     */
    Optional<RegistrationJpaEntity> findByTicketCode(String ticketCode);

    /**
     * Finds all registrations for a given event.
     *
     * @param eventId the event identifier
     * @return list of registration entities for the event
     */
    List<RegistrationJpaEntity> findByEventId(UUID eventId);

    /**
     * Finds all registrations associated with a given participant.
     *
     * @param participantId the participant identifier
     * @return list of registration entities for the participant
     */
    List<RegistrationJpaEntity> findByParticipantId(UUID participantId);

    /**
     * Checks whether a participant is already registered for a given event.
     *
     * @param eventId       the event identifier
     * @param participantId the participant identifier
     * @return true if a registration exists for the given event and participant
     */
    boolean existsByEventIdAndParticipantId(UUID eventId, UUID participantId);

    /**
     * Finds all CONFIRMED registrations for a given event, ordered by registration date ascending.
     *
     * @param eventId the event identifier
     * @return list of confirmed registration entities
     */
    @Query("SELECT r FROM RegistrationJpaEntity r WHERE r.eventId = :eventId AND r.status = 'CONFIRMED' ORDER BY r.registeredAt ASC")
    List<RegistrationJpaEntity> findConfirmedByEventId(@Param("eventId") UUID eventId);

    /**
     * Finds registrations in PENDING_PAYMENT status whose payment window has expired.
     * Card payments (null method or CARD) use the shorter cardThreshold window.
     * Cash/OXXO payments use the extended cashThreshold window.
     *
     * @param cardThreshold threshold for card-based pending payments
     * @param cashThreshold threshold for OXXO/cash pending payments
     * @return list of expired pending-payment registrations
     */
    @Query("""
            SELECT r FROM RegistrationJpaEntity r
            WHERE r.status = 'PENDING_PAYMENT'
              AND (
                (r.paymentMethod IS NULL OR r.paymentMethod = 'CARD') AND r.registeredAt < :cardThreshold
                OR
                r.paymentMethod IN ('OXXO', 'CASH_OTHER') AND r.registeredAt < :cashThreshold
              )
            """)
    List<RegistrationJpaEntity> findExpiredPendingPayments(
            @Param("cardThreshold") LocalDateTime cardThreshold,
            @Param("cashThreshold") LocalDateTime cashThreshold
    );
}
