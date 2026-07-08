package com.accesosport.payment.application.dto;

import java.math.BigDecimal;

public record PaymentStatusResponse(
        String paymentStatus,
        String paymentMethod,
        BigDecimal amountTotal,
        BigDecimal baseAmount,
        BigDecimal serviceFee
) {}
