package com.accesosport.payment.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrganizerFeeCalculatorTest {

    @Test
    void calculate_precioDebajoPuntoDeQuiebre_retornaMinimoFijo() {
        BigDecimal result = OrganizerFeeCalculator.calculate(new BigDecimal("100"));
        assertEquals(new BigDecimal("10.00"), result);
    }

    @Test
    void calculate_exactamenteEnElPuntoDeQuiebre_retornaMinimoFijo() {
        BigDecimal result = OrganizerFeeCalculator.calculate(new BigDecimal("200"));
        assertEquals(new BigDecimal("10.00"), result);
    }

    @Test
    void calculate_precioSobreElPuntoDeQuiebre_retornaCincoPorCiento() {
        BigDecimal result = OrganizerFeeCalculator.calculate(new BigDecimal("350"));
        assertEquals(new BigDecimal("17.50"), result);
    }
}
