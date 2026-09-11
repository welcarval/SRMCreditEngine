package com.srm.msbackend.repositories;

import com.srm.msbackend.entities.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {
    @Query("""
            select t from Transacao t
            where (:dataInicial is null or t.realizadaEm >= :dataInicial)
              and (:dataFinal is null or t.realizadaEm < :dataFinal)
              and (:fundoId is null or exists (
                    select f.id from Fundo f
                    where f.id = :fundoId
                      and (f.conta = t.contaOrigem or f.conta = t.contaDestino)
              ))
              and (:empresaId is null or exists (
                    select e.id from Empresa e
                    where e.id = :empresaId
                      and (e.conta = t.contaOrigem or e.conta = t.contaDestino)
              ))
              and (:moedaId is null
                   or t.contaOrigem.moeda.id = :moedaId
                   or t.contaDestino.moeda.id = :moedaId)
              and (:administrador = true or exists (
                    select permitido.id from Fundo permitido
                    where permitido.id in :fundosPermitidos
                      and (permitido.conta = t.contaOrigem or permitido.conta = t.contaDestino)
              ))
            order by t.realizadaEm desc
            """)
    List<Transacao> buscarExtrato(
            @Param("dataInicial") LocalDateTime dataInicial,
            @Param("dataFinal") LocalDateTime dataFinal,
            @Param("fundoId") Long fundoId,
            @Param("empresaId") Long empresaId,
            @Param("moedaId") Long moedaId,
            @Param("administrador") boolean administrador,
            @Param("fundosPermitidos") Collection<Long> fundosPermitidos);
}
