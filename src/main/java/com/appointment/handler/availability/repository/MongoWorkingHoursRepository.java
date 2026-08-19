package com.appointment.handler.availability.repository;

import com.appointment.handler.availability.entity.WorkingHours;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
@Profile("mongodb")
public interface MongoWorkingHoursRepository extends MongoRepository<WorkingHours, Long>, WorkingHoursRepository {
}
