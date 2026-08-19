package com.appointment.handler.business.repository;

import com.appointment.handler.business.entity.Business;
import com.appointment.handler.common.enums.BusinessStatus;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@Profile("jpa")
public interface JpaBusinessRepository extends JpaRepository<Business, Long>, BusinessRepository {

    @Override
    @Query("SELECT DISTINCT b FROM Business b " +
           "LEFT JOIN Branch br ON br.business = b " +
           "WHERE (:typeName IS NULL OR UPPER(b.businessType.name) = UPPER(:typeName)) " +
           "AND (:city IS NULL OR UPPER(br.city) = UPPER(:city)) " +
           "AND (:status IS NULL OR b.status = :status) " +
           "AND (:search IS NULL OR UPPER(b.name) LIKE UPPER(CONCAT('%', :search, '%')) OR UPPER(b.description) LIKE UPPER(CONCAT('%', :search, '%')))")
    Page<Business> findAllFiltered(
            @Param("typeName") String typeName,
            @Param("city") String city,
            @Param("status") BusinessStatus status,
            @Param("search") String search,
            Pageable pageable
    );
}
