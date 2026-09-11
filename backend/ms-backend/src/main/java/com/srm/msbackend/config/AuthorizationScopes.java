package com.srm.msbackend.config;

/** Scopes usados para autorizar as operações expostas pela API. */
public final class AuthorizationScopes {
    public static final String FUNDOS_READ = "fundos:read";
    public static final String FUNDOS_WRITE = "fundos:write";
    public static final String RECEBIVEIS_READ = "recebiveis:read";
    public static final String RECEBIVEIS_WRITE = "recebiveis:write";
    public static final String EMPRESAS_READ = "empresas:read";
    public static final String EMPRESAS_WRITE = "empresas:write";
    public static final String MOEDAS_READ = "moedas:read";
    public static final String MOEDAS_WRITE = "moedas:write";
    public static final String TIPOS_RECEBIVEIS_READ = "tipos-recebiveis:read";
    public static final String EXTRATOS_READ = "extratos:read";
    public static final String TRANSACOES_READ = "transacoes:read";
    public static final String TRANSACOES_WRITE = "transacoes:write";
    public static final String USUARIOS_READ = "usuarios:read";
    public static final String USUARIOS_WRITE = "usuarios:write";
    public static final String PERFIL_READ = "perfil:read";

    private AuthorizationScopes() {
    }
}
