package com.srm.msbackend.entities;

import jakarta.persistence.*;
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
        this.identificador = identificador;
        this.instituicao = instituicao;
        this.moeda = moeda;
    }

    public Long getId() { return id; }
    public String getIdentificador() { return identificador; }
    public void setIdentificador(String identificador) { this.identificador = identificador; }
    public String getInstituicao() { return instituicao; }
    public void setInstituicao(String instituicao) { this.instituicao = instituicao; }
    public Moeda getMoeda() { return moeda; }
    public void setMoeda(Moeda moeda) { this.moeda = moeda; }
    public Set<Transacao> getTransacoesOrigem() { return transacoesOrigem; }
    public Set<Transacao> getTransacoesDestino() { return transacoesDestino; }
}
