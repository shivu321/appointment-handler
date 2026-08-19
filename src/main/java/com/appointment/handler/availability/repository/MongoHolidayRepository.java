package com.appointment.handler.availability.repository;

import com.appointment.handler.availability.entity.Holiday;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
@Profile("mongodb")
public interface MongoHolidayRepository extends MongoRepository<Holiday, Long>, HolidayRepository {
}
