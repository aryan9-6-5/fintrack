package com.fintrack.fraud;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class FraudDetectionService {

    private static final BigDecimal FRAUD_MULTIPLIER = BigDecimal.valueOf(3);

    public boolean isFraudulent(BigDecimal amount, BigDecimal userAverage) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        if (userAverage == null || userAverage.compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }

        BigDecimal threshold = userAverage.multiply(FRAUD_MULTIPLIER);
        return amount.compareTo(threshold) >= 0;
    }
}
