package com.srm.msbackend.models;

import java.math.BigDecimal;

public record MoedaModel(
        Long id,
        String codigo,
        String nome,
        BigDecimal taxaCambioDolar
) {
}
