package com.appointment.handler.branch.repository;

import com.appointment.handler.branch.entity.Branch;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import java.util.List;

@NoRepositoryBean
public interface BranchRepository extends CrudRepository<Branch, Long> {
    List<Branch> findByBusinessId(Long businessId);
}
