package com.appointment.handler.payment.repository;

import com.appointment.handler.payment.entity.Payment;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
@Profile("mongodb")
public interface MongoPaymentRepository extends MongoRepository<Payment, Long>, PaymentRepository {
}
