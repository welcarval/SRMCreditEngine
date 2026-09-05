package com.srm.msbackend.repositories;

import com.srm.msbackend.entities.Moeda;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MoedaRepository extends JpaRepository<Moeda, Long> {
}
