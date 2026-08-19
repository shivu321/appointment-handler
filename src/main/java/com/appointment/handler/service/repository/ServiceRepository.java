package com.appointment.handler.service.repository;

import com.appointment.handler.service.entity.ServiceEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import java.util.List;

@NoRepositoryBean
public interface ServiceRepository extends CrudRepository<ServiceEntity, Long> {
    List<ServiceEntity> findByBusinessId(Long businessId);
    List<ServiceEntity> findAllById(Iterable<Long> ids);
}
