package com.srm.msbackend.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "fundo")
public class Fundo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String nome;
    @Column(unique = true)
    private String cnpj;
    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal taxaBase;
    @ManyToMany
    @JoinTable(name = "fundo_usuario",
        joinColumns = @JoinColumn(name = "fundo_id"),
        inverseJoinColumns = @JoinColumn(name = "usuario_id"))
    private Set<Usuario> usuarios = new HashSet<>();
    @OneToMany(mappedBy = "fundo", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Recebivel> recebiveis = new HashSet<>();

    protected Fundo() {
    }

    public Fundo(String nome, String cnpj, BigDecimal taxaBase) {
        this.nome = nome;
        this.cnpj = cnpj;
        this.taxaBase = taxaBase;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }
    public BigDecimal getTaxaBase() { return taxaBase; }
    public void setTaxaBase(BigDecimal taxaBase) { this.taxaBase = taxaBase; }
    public Set<Usuario> getUsuarios() { return usuarios; }
    public Set<Recebivel> getRecebiveis() { return recebiveis; }
}
