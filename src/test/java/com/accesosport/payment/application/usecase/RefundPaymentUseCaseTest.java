package com.accesosport.payment.application.usecase;

import com.accesosport.payment.domain.model.Payment;
import com.accesosport.payment.domain.model.PaymentStatus;
import com.accesosport.payment.domain.port.PaymentProcessorPort;
import com.accesosport.payment.domain.port.PaymentRepository;
import com.accesosport.registration.domain.model.PaymentMethod;
import com.accesosport.shared.domain.events.DomainEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefundPaymentUseCaseTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentProcessorPort paymentProcessorPort;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @Test
    void failedStripeRefundRemainsPersistableForRetry() {
        UUID registrationId = UUID.randomUUID();
        Payment payment = Payment.create(
                UUID.randomUUID(),
                registrationId,
                "cs_test_123",
                new BigDecimal("300.00"),
                new BigDecimal("30.00")
        );
        payment.confirm("pi_test_123", PaymentMethod.CARD);
        when(paymentRepository.findByRegistrationId(registrationId)).thenReturn(Optional.of(payment));
        when(paymentProcessorPort.refund("pi_test_123", payment.getId()))
                .thenThrow(new RuntimeException("temporary Stripe failure"));

        RefundPaymentUseCase useCase = new RefundPaymentUseCase(
                paymentRepository, paymentProcessorPort, domainEventPublisher);

        assertDoesNotThrow(() -> useCase.execute(new RefundPaymentUseCase.Command(registrationId, false)));
        assertEquals(PaymentStatus.REFUND_FAILED, payment.getStatus());
        assertEquals(1, payment.getRefundAttempts());
    }

    @Test
    void reembolsoParcial_descuenta8PorCientoDelTotal() {
        UUID registrationId = UUID.randomUUID();
        // base=$300, serviceFee=$30 → amountTotal=$330
        Payment payment = Payment.create(
                UUID.randomUUID(),
                registrationId,
                "cs_test_456",
                new BigDecimal("300.00"),
                new BigDecimal("30.00")
        );
        payment.confirm("pi_test_456", PaymentMethod.CARD);
        when(paymentRepository.findByRegistrationId(registrationId)).thenReturn(Optional.of(payment));
        when(paymentProcessorPort.refundPartial(anyString(), any(UUID.class), anyLong()))
                .thenReturn(new PaymentProcessorPort.RefundResult("re_partial_123"));

        RefundPaymentUseCase useCase = new RefundPaymentUseCase(
                paymentRepository, paymentProcessorPort, domainEventPublisher);

        useCase.execute(new RefundPaymentUseCase.Command(registrationId, true));

        // amountTotal=$330, 330 * 0.92 = 303.6 → HALF_DOWN → 303 MXN → 30300 centavos
        verify(paymentProcessorPort).refundPartial("pi_test_456", payment.getId(), 30300L);
        assertEquals(PaymentStatus.REFUNDED, payment.getStatus());
    }
}
