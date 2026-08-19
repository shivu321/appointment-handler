package com.appointment.handler.auth.repository;

import com.appointment.handler.auth.entity.Role;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Profile("jpa")
public interface JpaRoleRepository extends JpaRepository<Role, Long>, RoleRepository {
}
