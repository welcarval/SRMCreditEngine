package com.srm.msbackend.models;

import com.srm.msbackend.entities.StatusTransacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ExtratoTransacaoModel(
        Long id,
        BigDecimal valor,
        LocalDateTime realizadaEm,
        StatusTransacao status,
        Long contaOrigemId,
        Long contaDestinoId
) {
}
