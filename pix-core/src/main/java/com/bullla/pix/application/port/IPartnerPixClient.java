package com.bullla.pix.application.port;

import java.math.BigDecimal;

public interface IPartnerPixClient {

    PartnerResult sendPix(String transactionId, BigDecimal amount, String pixKey, String description);

    record PartnerResult(boolean success, String message) {
        public static PartnerResult ok(String message) {
            return new PartnerResult(true, message);
        }

        public static PartnerResult failure(String message) {
            return new PartnerResult(false, message);
        }
    }
}
