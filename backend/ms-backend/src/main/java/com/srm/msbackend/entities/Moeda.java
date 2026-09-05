package com.srm.msbackend.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "moeda")
public class Moeda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 3)
    private String codigo;
    @Column(nullable = false)
    private String nome;
    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal taxaCambioDolar;
    @OneToMany(mappedBy = "moeda")
    private Set<Conta> contas = new HashSet<>();

    protected Moeda() {
    }

    public Moeda(String codigo, String nome, BigDecimal taxaCambioDolar) {
        this.codigo = codigo;
        this.nome = nome;
        this.taxaCambioDolar = taxaCambioDolar;
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public BigDecimal getTaxaCambioDolar() { return taxaCambioDolar; }
    public void setTaxaCambioDolar(BigDecimal taxaCambioDolar) { this.taxaCambioDolar = taxaCambioDolar; }
    public Set<Conta> getContas() { return contas; }
}
