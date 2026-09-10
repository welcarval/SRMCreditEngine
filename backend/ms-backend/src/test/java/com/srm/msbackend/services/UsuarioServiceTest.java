package com.srm.msbackend.services;

import com.srm.msbackend.entities.*;
import com.srm.msbackend.models.UsuarioModel;
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
class UsuarioServiceTest {
    @Mock UsuarioRepository repository;
    @Mock TipoUsuarioRepository tipoRepository;
    @InjectMocks UsuarioService service;

    @Test void executaCrudEConverteAcesso() {
        TipoUsuario tipo = new TipoUsuario("OPERADOR", "Operador");
        Fundo fundo = new Fundo("F", "1", BigDecimal.ONE, null);
        Usuario usuario = new Usuario("Nome", "a@a", tipo);
        usuario.getFundos().add(fundo);
        UsuarioModel model = new UsuarioModel(1L, "Novo", "n@n", 2L);
        when(repository.findAll()).thenReturn(List.of(usuario));
        when(repository.findById(1L)).thenReturn(Optional.of(usuario));
        when(tipoRepository.findById(2L)).thenReturn(Optional.of(tipo));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(repository.existsById(1L)).thenReturn(true);
        assertThat(service.listar()).singleElement().satisfies(item -> {
            assertThat(item.tipo()).isEqualTo("OPERADOR");
            assertThat(item.fundoIds()).hasSize(1);
        });
        assertThat(service.buscarPorId(1L)).isPresent();
        assertThat(service.salvar(model).getEmail()).isEqualTo("n@n");
        assertThat(service.atualizar(1L, model)).isPresent();
        assertThat(service.deletar(1L)).isTrue();
    }

    @Test void trataTipoAusenteERegistroAusente() {
        when(tipoRepository.findById(2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.salvar(new UsuarioModel(null, "N", "e", 2L)))
                .isInstanceOf(IllegalArgumentException.class);
        when(repository.findById(9L)).thenReturn(Optional.empty());
        when(repository.existsById(9L)).thenReturn(false);
        assertThat(service.atualizar(9L, new UsuarioModel(9L, "N", "e", 2L))).isEmpty();
        assertThat(service.deletar(9L)).isFalse();
    }
}
