package com.srm.msbackend.models;

public record UsuarioModel(
        Long id,
        String nome,
        String email,
        java.util.List<Long> roleIds,
        Long tipoId
) {
    public UsuarioModel(Long id, String nome, String email, Long tipoId) {
        this(id, nome, email, java.util.List.of(), tipoId);
    }
}
