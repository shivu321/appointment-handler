package com.appointment.handler.availability.repository;

import com.appointment.handler.availability.entity.WorkingHours;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Profile("jpa")
public interface JpaWorkingHoursRepository extends JpaRepository<WorkingHours, Long>, WorkingHoursRepository {
}
