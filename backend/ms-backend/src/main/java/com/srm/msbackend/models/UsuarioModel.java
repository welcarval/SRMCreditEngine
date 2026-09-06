package com.srm.msbackend.models;

public record UsuarioModel(
        Long id,
        String nome,
        String email,
        Long tipoId
) {
}
