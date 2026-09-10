package com.srm.msbackend.models;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

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
        @NotNull(message = "A taxa base é obrigatória")
        @DecimalMin(value = "0", message = "A taxa base deve estar entre 0 e 100")
        @DecimalMax(value = "100", message = "A taxa base deve estar entre 0 e 100")
        BigDecimal taxaBase
) {
}
