package com.accesosport.payment.application.dto;

import java.math.BigDecimal;

public record PricingBreakdownResponse(
        BigDecimal basePrice,
        BigDecimal serviceFee,
        BigDecimal total
) {
}
