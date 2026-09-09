package com.srm.msbackend.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransacaoModel(
        Long id,
        BigDecimal valor,
        LocalDateTime realizadaEm,
        Long contaOrigemId,
        Long contaDestinoId
) {
}
