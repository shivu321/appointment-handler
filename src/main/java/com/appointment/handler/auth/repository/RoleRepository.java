package com.appointment.handler.auth.repository;

import com.appointment.handler.auth.entity.Role;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import java.util.Optional;

@NoRepositoryBean
public interface RoleRepository extends CrudRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
