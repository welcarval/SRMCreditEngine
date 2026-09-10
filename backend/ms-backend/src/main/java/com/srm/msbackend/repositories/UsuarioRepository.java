package com.srm.msbackend.repositories;

import com.srm.msbackend.entities.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    @EntityGraph(attributePaths = {"tipo", "fundos"})
    Optional<Usuario> findByEmailIgnoreCase(String email);
}
