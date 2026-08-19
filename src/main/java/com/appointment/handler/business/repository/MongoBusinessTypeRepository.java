package com.appointment.handler.business.repository;

import com.appointment.handler.business.entity.BusinessType;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
@Profile("mongodb")
public interface MongoBusinessTypeRepository extends MongoRepository<BusinessType, Long>, BusinessTypeRepository {
}
