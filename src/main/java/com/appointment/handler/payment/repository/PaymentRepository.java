package com.appointment.handler.payment.repository;

import com.appointment.handler.payment.entity.Payment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import java.util.List;

@NoRepositoryBean
public interface PaymentRepository extends CrudRepository<Payment, Long> {
    List<Payment> findByAppointmentId(Long appointmentId);
}
