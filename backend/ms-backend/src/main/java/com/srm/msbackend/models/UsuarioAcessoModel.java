package com.srm.msbackend.models;

import java.util.List;

public record UsuarioAcessoModel(
        Long id,
        String nome,
        String email,
        String tipo,
        List<Long> fundoIds
) {
}
