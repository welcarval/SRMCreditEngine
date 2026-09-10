package com.srm.msbackend.repositories;

import com.srm.msbackend.entities.TipoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TipoUsuarioRepository extends JpaRepository<TipoUsuario, Long> {
    Optional<TipoUsuario> findByCodigoIgnoreCase(String codigo);
}
