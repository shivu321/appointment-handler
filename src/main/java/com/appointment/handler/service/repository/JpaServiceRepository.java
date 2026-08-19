package com.appointment.handler.service.repository;

import com.appointment.handler.service.entity.ServiceEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Profile("jpa")
public interface JpaServiceRepository extends JpaRepository<ServiceEntity, Long>, ServiceRepository {
}
