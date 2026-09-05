package com.srm.msbackend.repositories;

import com.srm.msbackend.entities.Fundo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FundoRepository extends JpaRepository<Fundo, Long> {
}
