package com.srm.msbackend.repositories;

import com.srm.msbackend.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByCodigoIgnoreCase(String codigo);
}
