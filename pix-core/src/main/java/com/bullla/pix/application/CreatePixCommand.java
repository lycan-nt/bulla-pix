package com.bullla.pix.application;

import java.math.BigDecimal;

public record CreatePixCommand(
        String transactionId,
        BigDecimal amount,
        String pixKey,
        String description
) {
}
