package com.appointment.handler.business.repository;

import com.appointment.handler.business.entity.BusinessType;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Profile("jpa")
public interface JpaBusinessTypeRepository extends JpaRepository<BusinessType, Long>, BusinessTypeRepository {
}
