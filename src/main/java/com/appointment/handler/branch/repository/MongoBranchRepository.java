package com.appointment.handler.branch.repository;

import com.appointment.handler.branch.entity.Branch;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
@Profile("mongodb")
public interface MongoBranchRepository extends MongoRepository<Branch, Long>, BranchRepository {
}
