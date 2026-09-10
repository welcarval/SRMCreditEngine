package com.srm.msbackend.services;

import com.srm.msbackend.entities.Moeda;
import com.srm.msbackend.models.MoedaModel;
import com.srm.msbackend.repositories.MoedaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MoedaServiceTest {
    @Mock MoedaRepository repository;
    @InjectMocks MoedaService service;

    @Test void listaBuscaSalvaAtualizaEExclui() {
        Moeda moeda = new Moeda("BRL", "Real", BigDecimal.ONE);
        when(repository.findAll()).thenReturn(List.of(moeda));
        when(repository.findById(1L)).thenReturn(Optional.of(moeda));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.existsById(1L)).thenReturn(true);

        assertThat(service.listar()).hasSize(1);
        assertThat(service.buscarPorId(1L)).isPresent();
        assertThat(service.salvar(new MoedaModel(1L, "USD", "Dolar", BigDecimal.TWO)).codigo()).isEqualTo("USD");
        assertThat(service.atualizar(1L, new MoedaModel(1L, "EUR", "Euro", BigDecimal.TEN))).isPresent();
        assertThat(service.deletar(1L)).isTrue();
        verify(repository).deleteById(1L);
    }

    @Test void trataAusencias() {
        when(repository.findById(9L)).thenReturn(Optional.empty());
        when(repository.existsById(9L)).thenReturn(false);
        assertThat(service.buscarPorId(9L)).isEmpty();
        assertThat(service.atualizar(9L, new MoedaModel(9L, "X", "X", BigDecimal.ONE))).isEmpty();
        assertThat(service.deletar(9L)).isFalse();
    }
}
