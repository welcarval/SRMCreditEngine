package com.srm.msbackend.repositories;

import com.srm.msbackend.entities.Conta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContaRepository extends JpaRepository<Conta, Long> {
}
