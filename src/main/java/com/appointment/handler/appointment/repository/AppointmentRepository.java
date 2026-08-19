package com.appointment.handler.appointment.repository;

import com.appointment.handler.appointment.entity.Appointment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@NoRepositoryBean
public interface AppointmentRepository extends CrudRepository<Appointment, Long> {
    List<Appointment> findOverlappingAppointments(
            Long staffId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime
    );

    List<Appointment> findByCustomerId(Long customerId);
    List<Appointment> findByStaffIdAndDate(Long staffId, LocalDate date);
}
