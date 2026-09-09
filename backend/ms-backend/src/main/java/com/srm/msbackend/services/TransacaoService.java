package com.srm.msbackend.services;

import com.srm.msbackend.entities.Conta;
import com.srm.msbackend.entities.StatusTransacao;
import com.srm.msbackend.entities.Transacao;
import com.srm.msbackend.models.TransacaoModel;
import com.srm.msbackend.repositories.ContaRepository;
import com.srm.msbackend.repositories.TransacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TransacaoService {
    private final TransacaoRepository transacaoRepository;
    private final ContaRepository contaRepository;

    public TransacaoService(TransacaoRepository transacaoRepository,
                           ContaRepository contaRepository) {
        this.transacaoRepository = transacaoRepository;
        this.contaRepository = contaRepository;
    }

    public List<Transacao> listar() {
        return transacaoRepository.findAll();
    }

    public Optional<Transacao> buscarPorId(Long id) {
        return transacaoRepository.findById(id);
    }

    public void executar(Transacao transacao) {
        transacao.setStatus(StatusTransacao.PENDENTE);
        try {
            BigDecimal taxaOrigem = transacao.getContaOrigem().getMoeda().getTaxaCambioDolar();
            BigDecimal taxaDestino = transacao.getContaDestino().getMoeda().getTaxaCambioDolar();
            if (taxaOrigem == null || taxaOrigem.signum() <= 0
                    || taxaDestino == null || taxaDestino.signum() <= 0) {
                throw new IllegalStateException("As moedas devem possuir taxas de câmbio válidas");
            }

            BigDecimal valorDestino = transacao.getValor()
                    .multiply(taxaOrigem)
                    .divide(taxaDestino, 2, RoundingMode.HALF_UP);
            transacao.getContaOrigem().removerSaldo(transacao.getValor());
            transacao.getContaDestino().adicionarSaldo(valorDestino);
            transacao.setStatus(StatusTransacao.SUCESSO);
        } catch (RuntimeException exception) {
            transacao.setStatus(StatusTransacao.FALHA);
            throw exception;
        }
    }

    @Transactional
    public Transacao salvar(TransacaoModel model) {
        Conta contaOrigem = contaRepository.findById(model.contaOrigemId())
                .orElseThrow(() -> new IllegalArgumentException("Conta de origem não encontrada: " + model.contaOrigemId()));

        Conta contaDestino = contaRepository.findById(model.contaDestinoId())
                .orElseThrow(() -> new IllegalArgumentException("Conta de destino não encontrada: " + model.contaDestinoId()));

        Transacao transacao = new Transacao();
        transacao.setValor(model.valor());
        transacao.setRealizadaEm(model.realizadaEm() != null ? model.realizadaEm() : LocalDateTime.now());
        transacao.setContaOrigem(contaOrigem);
        transacao.setContaDestino(contaDestino);

        executar(transacao);

        return transacaoRepository.save(transacao);
    }

    public boolean deletar(Long id) {
        if (!transacaoRepository.existsById(id)) {
            return false;
        }

        transacaoRepository.deleteById(id);
        return true;
    }
}
