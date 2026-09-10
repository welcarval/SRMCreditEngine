package com.srm.msbackend.services;

import com.srm.msbackend.entities.*;
import com.srm.msbackend.models.TransacaoModel;
import com.srm.msbackend.repositories.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransacaoServiceTest {
    @Mock TransacaoRepository repository;
    @Mock ContaRepository contaRepository;
    @InjectMocks TransacaoService service;

    private Conta conta(String id, BigDecimal saldo, BigDecimal cambio) {
        return new Conta(id, "Banco", new Moeda("BRL", "Real", cambio), saldo);
    }

    @Test void executaTransacaoECrud() {
        Conta origem = conta("O", new BigDecimal("100"), BigDecimal.ONE);
        Conta destino = conta("D", BigDecimal.ZERO, BigDecimal.ONE);
        Transacao transacao = new Transacao(new BigDecimal("25"), LocalDateTime.now(), origem, destino);
        service.executar(transacao);
        assertThat(transacao.getStatus()).isEqualTo(StatusTransacao.SUCESSO);
        assertThat(origem.getSaldo()).isEqualByComparingTo("75");
        assertThat(destino.getSaldo()).isEqualByComparingTo("25");

        when(repository.findAll()).thenReturn(List.of(transacao));
        when(repository.findById(1L)).thenReturn(Optional.of(transacao));
        when(repository.existsById(1L)).thenReturn(true);
        when(contaRepository.findById(1L)).thenReturn(Optional.of(origem));
        when(contaRepository.findById(2L)).thenReturn(Optional.of(destino));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertThat(service.listar()).hasSize(1);
        assertThat(service.buscarPorId(1L)).isPresent();
        assertThat(service.salvar(new TransacaoModel(null, BigDecimal.TEN, null, 1L, 2L))).isNotNull();
        assertThat(service.deletar(1L)).isTrue();
    }

    @Test void falhaComCambioInvalidoSaldoInsuficienteEContasAusentes() {
        Conta origem = conta("O", new BigDecimal("10"), BigDecimal.ZERO);
        Conta destino = conta("D", BigDecimal.ZERO, BigDecimal.ONE);
        Transacao transacao = new Transacao(BigDecimal.ONE, LocalDateTime.now(), origem, destino);
        assertThatThrownBy(() -> service.executar(transacao)).isInstanceOf(IllegalStateException.class);
        assertThat(transacao.getStatus()).isEqualTo(StatusTransacao.FALHA);

        origem = conta("O", BigDecimal.ZERO, BigDecimal.ONE);
        destino = conta("D", BigDecimal.ZERO, BigDecimal.ONE);
        Transacao semSaldo = new Transacao(BigDecimal.ONE, LocalDateTime.now(), origem, destino);
        assertThatThrownBy(() -> service.executar(semSaldo)).isInstanceOf(IllegalArgumentException.class);
        assertThat(semSaldo.getStatus()).isEqualTo(StatusTransacao.FALHA);

        when(contaRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.salvar(new TransacaoModel(null, BigDecimal.ONE, null, 1L, 2L)))
                .isInstanceOf(IllegalArgumentException.class);
        when(repository.existsById(9L)).thenReturn(false);
        assertThat(service.deletar(9L)).isFalse();
    }
}
