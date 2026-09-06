package com.srm.msbackend.services;

import com.srm.msbackend.entities.Conta;
import com.srm.msbackend.entities.Recebivel;
import com.srm.msbackend.entities.Transacao;
import com.srm.msbackend.models.TransacaoModel;
import com.srm.msbackend.repositories.ContaRepository;
import com.srm.msbackend.repositories.RecebivelRepository;
import com.srm.msbackend.repositories.TransacaoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TransacaoService {
    private final TransacaoRepository transacaoRepository;
    private final ContaRepository contaRepository;
    private final RecebivelRepository recebivelRepository;

    public TransacaoService(TransacaoRepository transacaoRepository,
                           ContaRepository contaRepository,
                           RecebivelRepository recebivelRepository) {
        this.transacaoRepository = transacaoRepository;
        this.contaRepository = contaRepository;
        this.recebivelRepository = recebivelRepository;
    }

    public List<Transacao> listar() {
        return transacaoRepository.findAll();
    }

    public Optional<Transacao> buscarPorId(Long id) {
        return transacaoRepository.findById(id);
    }

    public Transacao salvar(TransacaoModel model) {
        Conta contaOrigem = contaRepository.findById(model.contaOrigemId())
                .orElseThrow(() -> new IllegalArgumentException("Conta de origem não encontrada: " + model.contaOrigemId()));

        Conta contaDestino = contaRepository.findById(model.contaDestinoId())
                .orElseThrow(() -> new IllegalArgumentException("Conta de destino não encontrada: " + model.contaDestinoId()));

        Recebivel recebivel = recebivelRepository.findById(model.recebivelId())
                .orElseThrow(() -> new IllegalArgumentException("Recebível não encontrado: " + model.recebivelId()));

        Transacao transacao = new Transacao();
        transacao.setValor(model.valor());
        transacao.setRealizadaEm(model.realizadaEm() != null ? model.realizadaEm() : LocalDateTime.now());
        transacao.setRecebivel(recebivel);
        transacao.setContaOrigem(contaOrigem);
        transacao.setContaDestino(contaDestino);

        contaOrigem.setSaldo(contaOrigem.getSaldo().subtract(model.valor()));
        contaDestino.setSaldo(contaDestino.getSaldo().add(model.valor()));

        contaRepository.save(contaOrigem);
        contaRepository.save(contaDestino);

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
