package com.fintrack.fraud;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class FraudDetectionServiceTest {

    private final FraudDetectionService fraudService = new FraudDetectionService();

    @Test
    @DisplayName("Should flag transaction exactly at 3x average")
    void shouldFlagExactlyAtThreshold() {
        boolean result = fraudService.isFraudulent(new BigDecimal("300"), new BigDecimal("100"));
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should flag transaction above 3x average")
    void shouldFlagAboveThreshold() {
        boolean result = fraudService.isFraudulent(new BigDecimal("301"), new BigDecimal("100"));
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should not flag transaction below 3x average")
    void shouldNotFlagBelowThreshold() {
        boolean result = fraudService.isFraudulent(new BigDecimal("299"), new BigDecimal("100"));
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should not flag when historical average is zero")
    void shouldNotFlagZeroAverage() {
        boolean result = fraudService.isFraudulent(new BigDecimal("500"), BigDecimal.ZERO);
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should not flag for first transaction (null average)")
    void shouldNotFlagFirstTransaction() {
        boolean result = fraudService.isFraudulent(new BigDecimal("500"), null);
        assertThat(result).isFalse();
    }
    
    @Test
    @DisplayName("Should not flag when amount is null")
    void shouldNotFlagWhenAmountIsNull() {
        boolean result = fraudService.isFraudulent(null, new BigDecimal("100"));
        assertThat(result).isFalse();
    }
}
