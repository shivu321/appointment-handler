package com.appointment.handler.staff.repository;

import com.appointment.handler.staff.entity.Staff;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface StaffRepository extends CrudRepository<Staff, Long> {
    List<Staff> findByBusinessId(Long businessId);
    List<Staff> findByBranchId(Long branchId);
    List<Staff> findByServiceId(Long serviceId);
    Optional<Staff> findByIdForUpdate(Long id);
}
