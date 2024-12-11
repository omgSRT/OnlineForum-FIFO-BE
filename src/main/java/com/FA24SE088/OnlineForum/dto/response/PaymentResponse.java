package com.FA24SE088.OnlineForum.dto.response;

import lombok.Builder;

public abstract class PaymentResponse {
    @Builder
    public static class VNPayResponse {
        public String code;
        public String message;
        public String paymentUrl;
    }
}
