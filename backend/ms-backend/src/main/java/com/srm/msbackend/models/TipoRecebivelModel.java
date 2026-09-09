package com.srm.msbackend.models;

import java.math.BigDecimal;

public record TipoRecebivelModel(
        Long id,
        String nome,
        BigDecimal spread
) {
}
