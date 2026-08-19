package com.appointment.handler.payment.service;

import com.appointment.handler.appointment.entity.Appointment;
import com.appointment.handler.appointment.enums.AppointmentStatus;
import com.appointment.handler.appointment.repository.AppointmentRepository;
import com.appointment.handler.auth.entity.User;
import com.appointment.handler.common.exception.AppException;
import com.appointment.handler.common.exception.ResourceNotFoundException;
import com.appointment.handler.payment.dto.PaymentRequest;
import com.appointment.handler.payment.dto.PaymentResponse;
import com.appointment.handler.payment.entity.Payment;
import com.appointment.handler.payment.enums.PaymentStatus;
import com.appointment.handler.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request, User currentUser) {
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + request.getAppointmentId()));

        // Verify access (Only customer, staff, business owner or SUPER_ADMIN can pay)
        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        boolean isOwner = appointment.getStaff().getBusiness().getOwner().getId().equals(currentUser.getId());
        boolean isStaff = appointment.getStaff().getUser().getId().equals(currentUser.getId());
        boolean isCustomer = appointment.getCustomer().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwner && !isStaff && !isCustomer) {
            throw new AppException("You do not have permission to pay for this appointment", "FORBIDDEN", HttpStatus.FORBIDDEN);
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new AppException("Cannot pay for a cancelled appointment", "BAD_REQUEST", HttpStatus.BAD_REQUEST);
        }

        // Mock gateway processing logic
        boolean isDeclined = request.getCardNumber() != null && request.getCardNumber().endsWith("4444");
        PaymentStatus paymentStatus = isDeclined ? PaymentStatus.FAILED : PaymentStatus.SUCCESSFUL;

        String txRef = "TX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Payment payment = Payment.builder()
                .appointment(appointment)
                .amount(appointment.getPrice())
                .paymentMethod(request.getPaymentMethod())
                .transactionReference(txRef)
                .status(paymentStatus)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        if (paymentStatus == PaymentStatus.SUCCESSFUL) {
            appointment.setStatus(AppointmentStatus.CONFIRMED);
            appointmentRepository.save(appointment);
        }

        return mapToResponse(savedPayment);
    }

    private PaymentResponse mapToResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .appointmentId(p.getAppointment().getId())
                .amount(p.getAmount())
                .transactionReference(p.getTransactionReference())
                .paymentMethod(p.getPaymentMethod())
                .status(p.getStatus().name())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
