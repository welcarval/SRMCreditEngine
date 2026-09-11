package com.srm.msbackend.entities;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuario")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @ManyToMany
    @JoinTable(
            name = "usuario_role",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "usuario_scope",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "scope_id")
    )
    private Set<Scope> scopes = new HashSet<>();

    @ManyToMany(mappedBy = "usuarios")
    private Set<Fundo> fundos = new HashSet<>();

    public Usuario() {
    }

    public Usuario(String nome, String email, Set<Role> roles) {
        this.nome = nome;
        this.email = email;
        this.roles = roles == null ? new HashSet<>() : roles;
    }

    public Usuario(String nome, String email, Role role) {
        this(nome, email, role == null ? Set.of() : Set.of(role));
    }

    /**
     * Compatibilidade de construção para clientes legados; o vínculo persistido é Role.
     */
    @Deprecated
    public Usuario(String nome, String email, TipoUsuario tipo) {
        this(nome, email, tipo == null ? null : new Role(tipo.getCodigo(), tipo.getDescricao()));
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles == null ? new HashSet<>() : roles;
    }

    public Set<Scope> getScopes() {
        return scopes;
    }

    public void setScopes(Set<Scope> scopes) {
        this.scopes = scopes == null ? new HashSet<>() : scopes;
    }

    @Deprecated
    public TipoUsuario getTipo() {
        return roles.stream()
                .findFirst()
                .map(role -> new TipoUsuario(role.getCodigo(), role.getDescricao()))
                .orElse(null);
    }

    public Set<Fundo> getFundos() {
        return fundos;
    }
}
