package com.srm.msbackend.services;

import com.srm.msbackend.entities.Transacao;
import com.srm.msbackend.models.ExtratoTransacaoModel;
import com.srm.msbackend.repositories.TransacaoRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExtratoService {
    private final TransacaoRepository transacaoRepository;
    private final FundoAuthorizationService authorizationService;

    public ExtratoService(TransacaoRepository transacaoRepository,
                          FundoAuthorizationService authorizationService) {
        this.transacaoRepository = transacaoRepository;
        this.authorizationService = authorizationService;
    }

    @Transactional(readOnly = true)
    public List<ExtratoTransacaoModel> listar(
            LocalDate dataInicial,
            LocalDate dataFinal,
            Long fundoId,
            Long empresaId,
            Long moedaId,
            Authentication authentication) {
        if (dataInicial != null && dataFinal != null && dataInicial.isAfter(dataFinal)) {
            throw new IllegalArgumentException("A data inicial não pode ser posterior à data final");
        }

        boolean administrador = authorizationService.ehAdministrador(authentication);
        List<Long> fundosPermitidos = administrador
                ? List.of(-1L)
                : authorizationService.usuarioAtual(authentication).getFundos().stream()
                .map(fundo -> fundo.getId())
                .toList();
        if (fundosPermitidos.isEmpty()) {
            fundosPermitidos = List.of(-1L);
        }

        LocalDateTime inicio = dataInicial == null ? null : dataInicial.atStartOfDay();
        LocalDateTime fim = dataFinal == null ? null : dataFinal.plusDays(1).atStartOfDay();

        return transacaoRepository.buscarExtrato(
                        inicio, fim, fundoId, empresaId, moedaId, administrador, fundosPermitidos)
                .stream()
                .map(this::toModel)
                .toList();
    }

    private ExtratoTransacaoModel toModel(Transacao transacao) {
        return new ExtratoTransacaoModel(
                transacao.getId(),
                transacao.getValor(),
                transacao.getRealizadaEm(),
                transacao.getStatus(),
                transacao.getContaOrigem() == null ? null : transacao.getContaOrigem().getId(),
                transacao.getContaDestino() == null ? null : transacao.getContaDestino().getId()
        );
    }
}
