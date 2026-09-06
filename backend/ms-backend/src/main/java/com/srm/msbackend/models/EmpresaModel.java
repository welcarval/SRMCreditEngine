package com.srm.msbackend.models;

public record EmpresaModel(
        Long id,
        String razaoSocial,
        String cnpj,
        Long contaId
) {
}
