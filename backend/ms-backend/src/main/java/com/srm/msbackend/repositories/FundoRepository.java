package com.srm.msbackend.repositories;

import com.srm.msbackend.entities.Fundo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FundoRepository extends JpaRepository<Fundo, Long> {
    Optional<Fundo> findByCnpj(String cnpj);
    Optional<Fundo> findByNome(String nome);
    Optional<Fundo> findByContaId(Long contaId);
}
