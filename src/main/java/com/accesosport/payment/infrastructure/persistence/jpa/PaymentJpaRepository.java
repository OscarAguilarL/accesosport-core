package com.accesosport.payment.infrastructure.persistence.jpa;

import com.accesosport.payment.infrastructure.persistence.entity.PaymentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentJpaRepository extends JpaRepository<PaymentJpaEntity, UUID> {

    Optional<PaymentJpaEntity> findByRegistrationId(UUID registrationId);

    Optional<PaymentJpaEntity> findByStripeSessionId(String stripeSessionId);
}
