package com.srm.msbackend.entities;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "empresa")
public class Empresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String razaoSocial;

    @Column(nullable = false, unique = true)
    private String cnpj;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
    @JoinColumn(name = "conta_id", nullable = false, unique = true)
    private Conta conta;

    @OneToMany(mappedBy = "empresa")
    private Set<Recebivel> recebiveis = new HashSet<>();

    public Empresa() {
    }

    public Empresa(String razaoSocial, String cnpj, Conta conta) {
        this.razaoSocial = razaoSocial;
        this.cnpj = cnpj;
        this.conta = conta;
    }

    public Long getId() {
        return id;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public Conta getConta() {
        return conta;
    }

    public void setConta(Conta conta) {
        this.conta = conta;
    }

    public Set<Recebivel> getRecebiveis() {
        return recebiveis;
    }
}
