package com.appointment.handler.business.repository;

import com.appointment.handler.business.entity.Business;
import com.appointment.handler.common.enums.BusinessStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

@NoRepositoryBean
public interface BusinessRepository extends CrudRepository<Business, Long>, PagingAndSortingRepository<Business, Long> {
    Page<Business> findAllFiltered(
            String typeName,
            String city,
            BusinessStatus status,
            String search,
            Pageable pageable
    );
}
