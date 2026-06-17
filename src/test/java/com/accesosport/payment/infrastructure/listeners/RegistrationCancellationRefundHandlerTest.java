package com.accesosport.payment.infrastructure.listeners;

import com.accesosport.payment.application.service.PaymentApplicationService;
import com.accesosport.payment.domain.model.Payment;
import com.accesosport.payment.domain.model.PaymentStatus;
import com.accesosport.payment.domain.port.PaymentRepository;
import com.accesosport.registration.domain.events.RegistrationCancelledEvent;
import com.accesosport.registration.domain.model.PaymentMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationCancellationRefundHandlerTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private PaymentApplicationService paymentApplicationService;
    @InjectMocks private RegistrationCancellationRefundHandler handler;

    @Test
    void masde15Dias_conPagoConfirmado_llamaReembolsoParcial() {
        UUID registrationId = UUID.randomUUID();
        Payment payment = Payment.create(UUID.randomUUID(), registrationId, "cs_test",
                new BigDecimal("300.00"), new BigDecimal("30.00"));
        payment.confirm("pi_test", PaymentMethod.CARD);
        when(paymentRepository.findByRegistrationId(registrationId)).thenReturn(Optional.of(payment));

        RegistrationCancelledEvent event = new RegistrationCancelledEvent(
                registrationId, UUID.randomUUID(), UUID.randomUUID(), 16);

        handler.handle(event);

        verify(paymentApplicationService).refundPaymentPartial(registrationId);
    }

    @Test
    void exactamente15Dias_noEmiteReembolso() {
        UUID registrationId = UUID.randomUUID();

        RegistrationCancelledEvent event = new RegistrationCancelledEvent(
                registrationId, UUID.randomUUID(), UUID.randomUUID(), 15);

        handler.handle(event);

        verifyNoInteractions(paymentRepository);
        verifyNoInteractions(paymentApplicationService);
    }

    @Test
    void menosDe15Dias_noEmiteReembolso() {
        UUID registrationId = UUID.randomUUID();

        RegistrationCancelledEvent event = new RegistrationCancelledEvent(
                registrationId, UUID.randomUUID(), UUID.randomUUID(), 5);

        handler.handle(event);

        verifyNoInteractions(paymentRepository);
        verifyNoInteractions(paymentApplicationService);
    }

    @Test
    void masde15Dias_pagoNOConfirmado_noEmiteReembolso() {
        UUID registrationId = UUID.randomUUID();
        Payment payment = Payment.create(UUID.randomUUID(), registrationId, "cs_test",
                new BigDecimal("300.00"), new BigDecimal("30.00"));
        when(paymentRepository.findByRegistrationId(registrationId)).thenReturn(Optional.of(payment));

        RegistrationCancelledEvent event = new RegistrationCancelledEvent(
                registrationId, UUID.randomUUID(), UUID.randomUUID(), 20);

        handler.handle(event);

        verifyNoInteractions(paymentApplicationService);
    }
}
