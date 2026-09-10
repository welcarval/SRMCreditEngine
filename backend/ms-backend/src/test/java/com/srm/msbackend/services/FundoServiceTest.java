package com.srm.msbackend.services;

import com.srm.msbackend.entities.*;
import com.srm.msbackend.models.*;
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
class FundoServiceTest {
    @Mock FundoRepository repository;
    @Mock ContaRepository contaRepository;
    @Mock RecebivelRepository recebivelRepository;
    @Mock TransacaoRepository transacaoRepository;
    @Mock TransacaoService transacaoService;
    @Mock UsuarioRepository usuarioRepository;
    @InjectMocks FundoService service;

    private final Moeda moeda = new Moeda("BRL", "Real", BigDecimal.ONE);
    private final Conta conta = new Conta("F", "Banco", moeda, new BigDecimal("1000"));
    private final Fundo fundo = new Fundo("Fundo", "1", new BigDecimal("0.1"), conta);
    private final Empresa empresa = new Empresa("Empresa", "2", new Conta("E", "Banco", moeda));
    private final TipoRecebivel tipo = new TipoRecebivel("Duplicata", new BigDecimal("0.02"));

    @Test void executaCrudEAssociacoes() {
        FundoModel model = new FundoModel(1L, "Novo", "3", BigDecimal.ONE, 4L, null);
        when(repository.findAll()).thenReturn(List.of(fundo));
        when(repository.findAllByUsuarios_EmailIgnoreCase("u@u")).thenReturn(List.of(fundo));
        when(repository.findById(1L)).thenReturn(Optional.of(fundo));
        when(contaRepository.findById(4L)).thenReturn(Optional.of(conta));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(repository.existsById(1L)).thenReturn(true);
        assertThat(service.listar()).hasSize(1);
        assertThat(service.listarPorUsuario("u@u")).hasSize(1);
        assertThat(service.buscarPorId(1L)).isPresent();
        assertThat(service.salvar(model).nome()).isEqualTo("Novo");
        assertThat(service.atualizar(1L, model)).isPresent();
        assertThat(service.deletar(1L)).isTrue();

        Usuario usuario = new Usuario("U", "u@u", new TipoUsuario("OPERADOR", "Operador"));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));
        service.associarUsuario(1L, 2L);
        assertThat(fundo.getUsuarios()).contains(usuario);
        service.removerUsuario(1L, 2L);
        assertThat(fundo.getUsuarios()).doesNotContain(usuario);
    }

    @Test void criaSemContaETrataContaAusente() {
        FundoModel semConta = new FundoModel(null, "F", "1", BigDecimal.ONE, null, null);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertThat(service.salvar(semConta).contaId()).isNull();
        when(contaRepository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.salvar(new FundoModel(null, "F", "1", BigDecimal.ONE, 9L, null)))
                .isInstanceOf(IllegalArgumentException.class);
        when(repository.findById(9L)).thenReturn(Optional.empty());
        assertThat(service.atualizar(9L, semConta)).isEmpty();
        when(repository.existsById(9L)).thenReturn(false);
        assertThat(service.deletar(9L)).isFalse();
    }

    @Test void compraRecebivelEValidaFalhas() {
        Recebivel livre = new Recebivel(new BigDecimal("100"), new BigDecimal("110"),
                LocalDate.now().plusDays(30), null, tipo, empresa);
        when(repository.findById(1L)).thenReturn(Optional.of(fundo));
        when(recebivelRepository.findById(2L)).thenReturn(Optional.of(livre));
        when(recebivelRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertThat(service.adicionarRecebivel(1L, 2L).fundoId()).isNull();
        verify(transacaoService).executar(any(Transacao.class));
        verify(transacaoRepository).save(any(Transacao.class));

        Recebivel associado = new Recebivel(BigDecimal.TEN, BigDecimal.TEN, LocalDate.now(), fundo, tipo, empresa);
        when(recebivelRepository.findById(3L)).thenReturn(Optional.of(associado));
        assertThatThrownBy(() -> service.adicionarRecebivel(1L, 3L)).isInstanceOf(IllegalStateException.class);

        Fundo semConta = new Fundo("S", "S", BigDecimal.ONE, null);
        when(repository.findById(4L)).thenReturn(Optional.of(semConta));
        when(recebivelRepository.findById(2L)).thenReturn(Optional.of(livre));
        assertThatThrownBy(() -> service.adicionarRecebivel(4L, 2L)).isInstanceOf(IllegalStateException.class);

        Empresa semContaEmpresa = new Empresa("E", "E", null);
        Recebivel semDestino = new Recebivel(BigDecimal.TEN, BigDecimal.TEN, LocalDate.now(), null, tipo, semContaEmpresa);
        when(repository.findById(1L)).thenReturn(Optional.of(fundo));
        when(recebivelRepository.findById(5L)).thenReturn(Optional.of(semDestino));
        assertThatThrownBy(() -> service.adicionarRecebivel(1L, 5L)).isInstanceOf(IllegalStateException.class);
    }

    @Test void trataAssociacoesInexistentes() {
        when(repository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.associarUsuario(9L, 1L)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.removerUsuario(9L, 1L)).isInstanceOf(IllegalArgumentException.class);
        when(repository.findById(1L)).thenReturn(Optional.of(fundo));
        when(usuarioRepository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.associarUsuario(1L, 9L)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.removerUsuario(1L, 9L)).isInstanceOf(IllegalArgumentException.class);
        when(recebivelRepository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.adicionarRecebivel(1L, 9L)).isInstanceOf(IllegalArgumentException.class);
    }
}
