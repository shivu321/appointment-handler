package com.appointment.handler.service.repository;

import com.appointment.handler.service.entity.ServiceEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
@Profile("mongodb")
public interface MongoServiceRepository extends MongoRepository<ServiceEntity, Long>, ServiceRepository {
}
