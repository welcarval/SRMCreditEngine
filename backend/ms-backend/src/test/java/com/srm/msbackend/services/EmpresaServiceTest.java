package com.srm.msbackend.services;

import com.srm.msbackend.entities.*;
import com.srm.msbackend.models.EmpresaModel;
import com.srm.msbackend.repositories.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceTest {
    @Mock EmpresaRepository repository;
    @Mock ContaRepository contaRepository;
    @InjectMocks EmpresaService service;

    private Conta conta() { return new Conta("C", "Banco", new Moeda("BRL", "Real", BigDecimal.ONE)); }

    @Test void executaCrud() {
        Conta conta = conta();
        Empresa empresa = new Empresa("Empresa", "1", conta);
        EmpresaModel model = new EmpresaModel(1L, "Nova", "2", 3L);
        when(contaRepository.findById(3L)).thenReturn(Optional.of(conta));
        when(repository.findAll()).thenReturn(List.of(empresa));
        when(repository.findById(1L)).thenReturn(Optional.of(empresa));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(repository.existsById(1L)).thenReturn(true);
        assertThat(service.listar()).hasSize(1);
        assertThat(service.buscarPorId(1L)).isPresent();
        assertThat(service.salvar(model).razaoSocial()).isEqualTo("Nova");
        assertThat(service.atualizar(1L, model)).isPresent();
        assertThat(service.deletar(1L)).isTrue();
    }

    @Test void trataAusenciasEContaInexistente() {
        when(repository.findById(9L)).thenReturn(Optional.empty());
        when(repository.existsById(9L)).thenReturn(false);
        assertThat(service.buscarPorId(9L)).isEmpty();
        assertThat(service.atualizar(9L, new EmpresaModel(9L, "X", "X", 1L))).isEmpty();
        assertThat(service.deletar(9L)).isFalse();
        when(contaRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.salvar(new EmpresaModel(null, "X", "X", 1L)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
