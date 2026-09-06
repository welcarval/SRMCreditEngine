package com.srm.msbackend.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "tipo_recebivel")
public class TipoRecebivel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private BigDecimal spread;

    public TipoRecebivel() {
    }

    public TipoRecebivel(String nome, BigDecimal spread) {
        this.nome = nome;
        this.spread = spread;
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

    public BigDecimal getSpread() {
        return spread;
    }

    public void setSpread(BigDecimal spread) {
        this.spread = spread;
    }
}
