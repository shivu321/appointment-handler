package com.appointment.handler.business.repository;

import com.appointment.handler.business.entity.BusinessType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import java.util.Optional;

@NoRepositoryBean
public interface BusinessTypeRepository extends CrudRepository<BusinessType, Long> {
    Optional<BusinessType> findByName(String name);
}
