package com.appointment.handler.staff.repository;

import com.appointment.handler.staff.entity.Staff;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
@Profile("mongodb")
public interface MongoStaffRepository extends MongoRepository<Staff, Long>, StaffRepository {

    @Override
    @Query("{ 'services': { '$elemMatch': { 'id': ?0 } } }")
    List<Staff> findByServiceId(Long serviceId);

    @Override
    default Optional<Staff> findByIdForUpdate(Long id) {
        return findById(id);
    }
}
