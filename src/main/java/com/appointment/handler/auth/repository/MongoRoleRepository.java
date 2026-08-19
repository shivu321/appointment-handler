package com.appointment.handler.auth.repository;

import com.appointment.handler.auth.entity.Role;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
@Profile("mongodb")
public interface MongoRoleRepository extends MongoRepository<Role, Long>, RoleRepository {
}
