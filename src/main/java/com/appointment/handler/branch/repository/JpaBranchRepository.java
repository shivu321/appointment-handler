package com.appointment.handler.branch.repository;

import com.appointment.handler.branch.entity.Branch;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Profile("jpa")
public interface JpaBranchRepository extends JpaRepository<Branch, Long>, BranchRepository {
}
