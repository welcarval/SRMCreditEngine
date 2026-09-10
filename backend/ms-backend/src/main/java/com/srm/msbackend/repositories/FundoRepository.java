package com.srm.msbackend.repositories;

import com.srm.msbackend.entities.Fundo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FundoRepository extends JpaRepository<Fundo, Long> {
    Optional<Fundo> findByCnpj(String cnpj);
    Optional<Fundo> findByNome(String nome);
    Optional<Fundo> findByContaId(Long contaId);
    List<Fundo> findAllByUsuarios_EmailIgnoreCase(String email);

    List<Fundo> findAllByUsuarios_Id(Long usuarioId);

    boolean existsByIdAndUsuarios_Id(Long fundoId, Long usuarioId);
}
