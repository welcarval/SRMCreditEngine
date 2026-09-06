package com.srm.msbackend.models;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecebivelModel(
        Long id,
        BigDecimal valorFace,
        BigDecimal valorPresente,
        LocalDate dataVencimento,
        Long fundoId,
        Long tipoId,
        Long empresaId,
        BigDecimal prazo,
        BigDecimal spread,
        BigDecimal taxaBase
) {
}
