package com.appointment.handler.payment.controller;

import com.appointment.handler.auth.entity.User;
import com.appointment.handler.common.response.ResponseDto;
import com.appointment.handler.payment.dto.PaymentRequest;
import com.appointment.handler.payment.dto.PaymentResponse;
import com.appointment.handler.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseDto<PaymentResponse> processPayment(
            @Valid @RequestBody PaymentRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        PaymentResponse response = paymentService.processPayment(request, currentUser);
        return ResponseDto.success("Payment processed successfully", response);
    }
}
