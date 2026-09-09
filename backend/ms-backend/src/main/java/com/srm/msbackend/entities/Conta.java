package com.srm.msbackend.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "conta")
public class Conta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String identificador;

    @Column(nullable = false)
    private String instituicao;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saldo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "moeda_id", nullable = false)
    private Moeda moeda;

    @OneToMany(mappedBy = "contaOrigem")
    private Set<Transacao> transacoesOrigem = new HashSet<>();

    @OneToMany(mappedBy = "contaDestino")
    private Set<Transacao> transacoesDestino = new HashSet<>();

    protected Conta() {
    }

    public Conta(String identificador, String instituicao, Moeda moeda) {
        this(identificador, instituicao, moeda, BigDecimal.ZERO);
    }

    public Conta(String identificador, String instituicao, Moeda moeda, BigDecimal saldo) {
        this.identificador = identificador;
        this.instituicao = instituicao;
        this.moeda = moeda;
        this.saldo = saldo != null ? saldo : BigDecimal.ZERO;
    }

    public Long getId() {
        return id;
    }

    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }

    public String getInstituicao() {
        return instituicao;
    }

    public void setInstituicao(String instituicao) {
        this.instituicao = instituicao;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void adicionarSaldo(BigDecimal valor) {
        validarValorMovimentacao(valor);
        saldo = saldo.add(valor);
    }

    public void removerSaldo(BigDecimal valor) {
        validarValorMovimentacao(valor);
        if (saldo.compareTo(valor) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente para realizar a operação");
        }
        saldo = saldo.subtract(valor);
    }

    private void validarValorMovimentacao(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("O valor da movimentação deve ser maior que zero");
        }
    }

    public Moeda getMoeda() {
        return moeda;
    }

    public void setMoeda(Moeda moeda) {
        this.moeda = moeda;
    }

    public Set<Transacao> getTransacoesOrigem() {
        return transacoesOrigem;
    }

    public Set<Transacao> getTransacoesDestino() {
        return transacoesDestino;
    }
}
