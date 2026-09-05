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
    @OneToMany(mappedBy = "empresa")
    private Set<Recebivel> recebiveis = new HashSet<>();

    protected Empresa() {
    }

    public Empresa(String razaoSocial, String cnpj) {
        this.razaoSocial = razaoSocial;
        this.cnpj = cnpj;
    }

    public Long getId() { return id; }
    public String getRazaoSocial() { return razaoSocial; }
    public void setRazaoSocial(String razaoSocial) { this.razaoSocial = razaoSocial; }
    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }
    public Set<Recebivel> getRecebiveis() { return recebiveis; }
}
