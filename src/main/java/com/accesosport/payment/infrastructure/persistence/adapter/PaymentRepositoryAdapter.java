package com.accesosport.payment.infrastructure.persistence.adapter;

import com.accesosport.payment.domain.model.Payment;
import com.accesosport.payment.domain.port.PaymentRepository;
import com.accesosport.payment.infrastructure.persistence.jpa.PaymentJpaRepository;
import com.accesosport.payment.infrastructure.persistence.mapper.PaymentMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@AllArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;

    @Override
    public void save(Payment payment) {
        paymentJpaRepository.save(PaymentMapper.toEntity(payment));
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        return paymentJpaRepository.findById(id)
                .map(PaymentMapper::toDomain);
    }

    @Override
    public Optional<Payment> findByRegistrationId(UUID registrationId) {
        return paymentJpaRepository.findByRegistrationId(registrationId)
                .map(PaymentMapper::toDomain);
    }

    @Override
    public Optional<Payment> findByStripeSessionId(String stripeSessionId) {
        return paymentJpaRepository.findByStripeSessionId(stripeSessionId)
                .map(PaymentMapper::toDomain);
    }
}
