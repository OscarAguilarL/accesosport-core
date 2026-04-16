package com.accesosport.registration.domain.repository;

import com.accesosport.registration.domain.model.Registration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RegistrationRepository {

    /**
     * Finds a registration by its unique identifier.
     *
     * @param id the registration identifier
     * @return the registration if found, otherwise empty
     */
    Optional<Registration> findById(UUID id);

    /**
     * Finds a registration by its unique ticket code.
     *
     * @param ticketCode the generated ticket code (e.g. "ACSP-4X7K")
     * @return the registration if found, otherwise empty
     */
    Optional<Registration> findByTicketCode(String ticketCode);

    /**
     * Persists the given registration. Creates it if new, updates it if existing.
     *
     * @param registration the registration to persist
     * @return the saved registration
     */
    Registration save(Registration registration);

    /**
     * Persists a batch of registrations in a single operation.
     *
     * @param registrations the list of registrations to persist
     */
    void saveAll(List<Registration> registrations);

    /**
     * Finds all registrations for a given event.
     *
     * @param eventId the event identifier
     * @return list of registrations for the event, or empty list if none
     */
    List<Registration> findByEventId(UUID eventId);

    /**
     * Finds all CONFIRMED registrations for a given event.
     *
     * @param eventId the event identifier
     * @return list of confirmed registrations for the event, or empty list if none
     */
    List<Registration> findConfirmedByEventId(UUID eventId);

    /**
     * Finds all registrations associated with a given participant.
     *
     * @param participantId the participant identifier
     * @return list of registrations for the participant, or empty list if none
     */
    List<Registration> findByParticipantId(UUID participantId);

    /**
     * Checks whether a participant is already registered for a given event.
     *
     * @param eventId       the event identifier
     * @param participantId the participant identifier
     * @return true if a registration exists for the given event and participant
     */
    boolean existsByEventIdAndParticipantId(UUID eventId, UUID participantId);

    /**
     * Finds registrations in PENDING_PAYMENT status whose payment window has expired.
     * Card payments use a shorter threshold; cash/OXXO payments use a longer one.
     *
     * @param cardThreshold threshold for card-based pending payments (shorter window)
     * @param cashThreshold threshold for OXXO/cash pending payments (extended window)
     * @return list of expired pending-payment registrations to be cancelled
     */
    List<Registration> findExpiredPendingPayments(LocalDateTime cardThreshold, LocalDateTime cashThreshold);
}
