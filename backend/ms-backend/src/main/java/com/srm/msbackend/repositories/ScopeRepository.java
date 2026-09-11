package com.srm.msbackend.repositories;

import com.srm.msbackend.entities.Scope;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScopeRepository extends JpaRepository<Scope, Long> {
    Optional<Scope> findByCodigoIgnoreCase(String codigo);
}
