package com.accesosport.payment.infrastructure.persistence.mapper;

import com.accesosport.payment.domain.model.Payment;
import com.accesosport.payment.domain.model.PaymentStatus;
import com.accesosport.payment.infrastructure.persistence.entity.PaymentJpaEntity;
import com.accesosport.registration.domain.model.PaymentMethod;

public class PaymentMapper {

    private PaymentMapper() {}

    public static Payment toDomain(PaymentJpaEntity entity) {
        if (entity == null) return null;

        return Payment.reconstitute(
                entity.getId(),
                entity.getRegistrationId(),
                entity.getStripeSessionId(),
                entity.getStripePaymentIntentId(),
                entity.getStripeRefundId(),
                entity.getBaseAmount(),
                entity.getServiceFeeAmount(),
                entity.getAmountTotal(),
                entity.getCurrency(),
                entity.getPaymentMethod() != null ? PaymentMethod.valueOf(entity.getPaymentMethod()) : null,
                PaymentStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getConfirmedAt(),
                entity.getRefundedAt()
        );
    }

    public static PaymentJpaEntity toEntity(Payment domain) {
        if (domain == null) return null;

        PaymentJpaEntity entity = new PaymentJpaEntity();
        entity.setId(domain.getId());
        entity.setRegistrationId(domain.getRegistrationId());
        entity.setStripeSessionId(domain.getStripeSessionId());
        entity.setStripePaymentIntentId(domain.getStripePaymentIntentId());
        entity.setStripeRefundId(domain.getStripeRefundId());
        entity.setBaseAmount(domain.getBaseAmount());
        entity.setServiceFeeAmount(domain.getServiceFeeAmount());
        entity.setAmountTotal(domain.getAmountTotal());
        entity.setCurrency(domain.getCurrency());
        entity.setPaymentMethod(domain.getPaymentMethod() != null ? domain.getPaymentMethod().name() : null);
        entity.setStatus(domain.getStatus().name());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setConfirmedAt(domain.getConfirmedAt());
        entity.setRefundedAt(domain.getRefundedAt());
        return entity;
    }
}
