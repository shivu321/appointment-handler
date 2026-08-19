package com.appointment.handler.appointment.repository;

import com.appointment.handler.appointment.entity.Appointment;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
@Profile("mongodb")
public interface MongoAppointmentRepository extends MongoRepository<Appointment, Long>, AppointmentRepository {

    @Override
    @Query("{ 'staff.id': ?0, 'date': ?1, 'status': { '$ne': 'CANCELLED' }, 'startTime': { '$lt': ?3 }, 'endTime': { '$gt': ?2 } }")
    List<Appointment> findOverlappingAppointments(
            Long staffId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime
    );
}
