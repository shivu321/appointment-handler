package com.appointment.handler.availability.repository;

import com.appointment.handler.availability.entity.Holiday;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Profile("jpa")
public interface JpaHolidayRepository extends JpaRepository<Holiday, Long>, HolidayRepository {
}
