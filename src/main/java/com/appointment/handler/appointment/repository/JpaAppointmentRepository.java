package com.appointment.handler.appointment.repository;

import com.appointment.handler.appointment.entity.Appointment;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
@Profile("jpa")
public interface JpaAppointmentRepository extends JpaRepository<Appointment, Long>, AppointmentRepository {

    @Override
    @Query("SELECT a FROM Appointment a WHERE a.staff.id = :staffId AND a.date = :date " +
           "AND a.status <> 'CANCELLED' " +
           "AND a.startTime < :endTime AND a.endTime > :startTime")
    List<Appointment> findOverlappingAppointments(
            @Param("staffId") Long staffId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );
}
