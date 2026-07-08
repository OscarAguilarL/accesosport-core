package com.accesosport.payment.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServiceFeeCalculatorTest {

    @Test
    void calculate_precioSobreElPuntoDeQuiebre_retornaDiezPorCiento() {
        BigDecimal result = ServiceFeeCalculator.calculate(new BigDecimal("300"));
        assertEquals(new BigDecimal("30.00"), result);
    }

    @Test
    void calculate_exactamenteEnElPuntoDeQuiebre_retornaMinimoFijo() {
        BigDecimal result = ServiceFeeCalculator.calculate(new BigDecimal("200"));
        assertEquals(new BigDecimal("20.00"), result);
    }

    @Test
    void calculate_precioDebajoPuntoDeQuiebre_retornaMinimoFijo() {
        BigDecimal result = ServiceFeeCalculator.calculate(new BigDecimal("150"));
        assertEquals(new BigDecimal("20.00"), result);
    }
}
