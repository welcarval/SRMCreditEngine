package com.srm.msbackend.models;

import java.math.BigDecimal;

public record FundoModel(
        Long id,
        String nome,
        String cnpj,
        BigDecimal taxaBase,
        Long contaId,
        BigDecimal saldo
) {
}
