package com.accesosport.payment.domain.port;

import com.accesosport.payment.domain.model.Payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {

    void save(Payment payment);

    Optional<Payment> findByRegistrationId(UUID registrationId);

    Optional<Payment> findByStripeSessionId(String stripeSessionId);
}
