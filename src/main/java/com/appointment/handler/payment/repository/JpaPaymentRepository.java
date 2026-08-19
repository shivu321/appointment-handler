package com.appointment.handler.payment.repository;

import com.appointment.handler.payment.entity.Payment;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Profile("jpa")
public interface JpaPaymentRepository extends JpaRepository<Payment, Long>, PaymentRepository {
}
