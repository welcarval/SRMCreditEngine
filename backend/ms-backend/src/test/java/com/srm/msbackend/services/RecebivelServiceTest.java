package com.srm.msbackend.services;

import com.srm.msbackend.entities.*;
import com.srm.msbackend.models.RecebivelModel;
import com.srm.msbackend.repositories.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecebivelServiceTest {
    @Mock RecebivelRepository repository;
    @Mock FundoRepository fundoRepository;
    @Mock EmpresaRepository empresaRepository;
    @Mock TipoRecebivelRepository tipoRepository;
    @InjectMocks RecebivelService service;

    private final TipoRecebivel tipo = new TipoRecebivel("Duplicata", new BigDecimal("0.02"));
    private final Empresa empresa = new Empresa("Empresa", "1", null);
    private final Fundo fundo = new Fundo("Fundo", "2", new BigDecimal("0.10"), null);

    private Recebivel recebivel(Fundo fundo) {
        return new Recebivel(new BigDecimal("90"), new BigDecimal("100"),
                LocalDate.now().plusDays(365), fundo, tipo, empresa);
    }

    @Test void listaBuscaSalvaAtualizaEExclui() {
        Recebivel existente = recebivel(fundo);
        RecebivelModel model = new RecebivelModel(1L, new BigDecimal("200"), null,
                LocalDate.now().plusDays(30), 1L, 2L, 3L, null, null, new BigDecimal("0.05"));
        when(repository.findAll()).thenReturn(List.of(existente));
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(fundoRepository.findById(1L)).thenReturn(Optional.of(fundo));
        when(empresaRepository.findById(3L)).thenReturn(Optional.of(empresa));
        when(tipoRepository.findById(2L)).thenReturn(Optional.of(tipo));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(repository.existsById(1L)).thenReturn(true);
        when(fundoRepository.findAllByUsuarios_Id(7L)).thenReturn(List.of(fundo));
        assertThat(service.listar()).hasSize(1);
        assertThat(service.buscarPorId(1L)).isPresent();
        assertThat(service.salvar(model).valorFace()).isEqualByComparingTo("200");
        assertThat(service.atualizar(1L, model)).isPresent();
        assertThat(service.listarParaUsuario(7L)).hasSize(1);
        assertThat(service.deletar(1L)).isTrue();
    }

    @Test void permiteRecebivelSemFundoEFiltraFundoNaoAutorizado() {
        Recebivel livre = recebivel(null);
        Fundo autorizado = mock(Fundo.class);
        Fundo outroFundo = mock(Fundo.class);
        when(autorizado.getId()).thenReturn(1L);
        when(outroFundo.getId()).thenReturn(99L);
        Recebivel outro = recebivel(outroFundo);
        when(repository.findAll()).thenReturn(List.of(livre, outro));
        when(fundoRepository.findAllByUsuarios_Id(7L)).thenReturn(List.of(autorizado));
        assertThat(service.listarParaUsuario(7L)).hasSize(1);
        assertThat(service.listarParaUsuario(7L).get(0).fundoId()).isNull();
    }

    @Test void cobreCalculosENaoEncontrados() {
        assertThat(service.calcularValorPresente(null, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE))
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(service.calcularValorPresente(BigDecimal.TEN, null, null, null)).isEqualByComparingTo("10");
        assertThat(service.calcularValorPresente(
                new BigDecimal("100"), new BigDecimal("0.10"),
                new BigDecimal("0.02"), new BigDecimal("0.03"), BigDecimal.ONE))
                .isEqualByComparingTo("86.96");
        assertThat(service.calcularPrazoEmAnos(null)).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(service.calcularPrazoEmAnos(LocalDate.now().minusDays(1))).isEqualByComparingTo(BigDecimal.ZERO);

        RecebivelModel model = new RecebivelModel(null, BigDecimal.TEN, null,
                LocalDate.now(), 1L, 2L, 3L, null, null, new BigDecimal("0.05"));
        when(fundoRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.salvar(model))
                .isInstanceOf(IllegalArgumentException.class);

        RecebivelModel semFundo = new RecebivelModel(null, BigDecimal.TEN, null,
                LocalDate.now(), null, 2L, 3L, null, null, BigDecimal.ONE);
        when(empresaRepository.findById(3L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.salvar(semFundo))
                .isInstanceOf(IllegalArgumentException.class);

        RecebivelModel semTaxaBase = new RecebivelModel(null, BigDecimal.TEN, null,
                LocalDate.now(), null, 2L, 3L, null, null, null);
        assertThatThrownBy(() -> service.salvar(semTaxaBase))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("taxa base");
    }
}
