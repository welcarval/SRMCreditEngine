package com.srm.msbackend.entities;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "scope")
public class Scope {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String codigo;

    @Column(nullable = false)
    private String descricao;

    @ManyToMany(mappedBy = "scopes")
    private Set<Role> roles = new HashSet<>();

    public Scope() {
    }

    public Scope(String codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Set<Role> getRoles() {
        return roles;
    }
}
