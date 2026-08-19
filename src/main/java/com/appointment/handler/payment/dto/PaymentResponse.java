package com.appointment.handler.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private Long appointmentId;
    private BigDecimal amount;
    private String transactionReference;
    private String paymentMethod;
    private String status;
    private LocalDateTime createdAt;
}
