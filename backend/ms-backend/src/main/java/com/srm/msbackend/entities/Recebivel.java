package com.srm.msbackend.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
@Entity
@Table(name = "recebivel")
public class Recebivel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(precision = 19, scale = 2)
    private BigDecimal valorPresente;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valorFace;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal taxaBase;

    private LocalDate dataVencimento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fundo_id")
    private Fundo fundo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tipo_id", nullable = false)
    private TipoRecebivel tipo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    public Recebivel() {
    }

    public Recebivel(BigDecimal valorPresente, BigDecimal valorFace, LocalDate dataVencimento, Fundo fundo,
                     TipoRecebivel tipo, Empresa empresa) {
        this(valorPresente, valorFace, dataVencimento, fundo, tipo, empresa, BigDecimal.ZERO);
    }

    public Recebivel(BigDecimal valorPresente, BigDecimal valorFace, LocalDate dataVencimento, Fundo fundo,
                     TipoRecebivel tipo, Empresa empresa, BigDecimal taxaBase) {
        this.valorPresente = valorPresente;
        this.valorFace = valorFace;
        this.dataVencimento = dataVencimento;
        this.fundo = fundo;
        this.tipo = tipo;
        this.empresa = empresa;
        this.taxaBase = taxaBase;
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getValorPresente() {
        return valorPresente;
    }

    public void setValorPresente(BigDecimal valorPresente) {
        this.valorPresente = valorPresente;
    }

    public BigDecimal getValorFace() {
        return valorFace;
    }

    public void setValorFace(BigDecimal valorFace) {
        this.valorFace = valorFace;
    }

    public BigDecimal getTaxaBase() {
        return taxaBase;
    }

    public void setTaxaBase(BigDecimal taxaBase) {
        this.taxaBase = taxaBase;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public void setDataVencimento(LocalDate dataVencimento) {
        this.dataVencimento = dataVencimento;
    }

    public Fundo getFundo() {
        return fundo;
    }

    public void setFundo(Fundo fundo) {
        this.fundo = fundo;
    }

    public TipoRecebivel getTipo() {
        return tipo;
    }

    public void setTipo(TipoRecebivel tipo) {
        this.tipo = tipo;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

}
