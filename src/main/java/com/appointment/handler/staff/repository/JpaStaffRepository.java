package com.appointment.handler.staff.repository;

import com.appointment.handler.staff.entity.Staff;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
@Profile("jpa")
public interface JpaStaffRepository extends JpaRepository<Staff, Long>, StaffRepository {

    @Override
    @Query("SELECT DISTINCT s FROM Staff s JOIN s.services ser WHERE ser.id = :serviceId")
    List<Staff> findByServiceId(@Param("serviceId") Long serviceId);

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Staff s WHERE s.id = :id")
    Optional<Staff> findByIdForUpdate(@Param("id") Long id);
}
