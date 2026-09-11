package com.srm.msbackend.models;

import java.util.List;

public record UsuarioAcessoModel(
        Long id,
        String nome,
        String email,
        List<String> roles,
        List<String> scopes,
        List<Long> fundoIds,
        List<String> directScopes
) {
    public UsuarioAcessoModel(Long id, String nome, String email, String tipo, List<Long> fundoIds) {
        this(id, nome, email,
                tipo == null ? List.of() : List.of(tipo),
                List.of(),
                fundoIds,
                List.of());
    }

    public String tipo() {
        return roles.isEmpty() ? null : roles.get(0);
    }
}
