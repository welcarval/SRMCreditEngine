package com.srm.msbackend.repositories;

import com.srm.msbackend.entities.Conta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContaRepository extends JpaRepository<Conta, Long> {
    Optional<Conta> findByIdentificador(String identificador);
}
