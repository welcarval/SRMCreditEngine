package com.srm.msbackend.services;

import com.srm.msbackend.entities.Fundo;
import com.srm.msbackend.entities.TipoUsuario;
import com.srm.msbackend.entities.Transacao;
import com.srm.msbackend.entities.Usuario;
import com.srm.msbackend.repositories.TransacaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExtratoServiceTest {
    @Mock TransacaoRepository repository;
    @Mock FundoAuthorizationService authorizationService;
    @Mock Authentication authentication;
    @Mock Fundo fundo;
    @InjectMocks ExtratoService service;

    @Test
    void consultaExtratoComoAdministrador() {
        when(authorizationService.ehAdministrador(authentication)).thenReturn(true);
        when(repository.buscarExtrato(any(), any(), eq(1L), eq(2L), eq(3L), eq(true), any()))
                .thenReturn(List.of(new Transacao()));

        service.listar(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31),
                1L, 2L, 3L, authentication);

        verify(repository).buscarExtrato(
                eq(LocalDate.of(2026, 1, 1).atStartOfDay()),
                eq(LocalDate.of(2026, 2, 1).atStartOfDay()),
                eq(1L), eq(2L), eq(3L), eq(true), eq(List.of(-1L)));
    }

    @Test
    void consultaExtratoDoOperadorSomenteNosFundosPermitidos() {
        Usuario usuario = new Usuario("Operador", "op@local",
                new TipoUsuario("OPERADOR", "Operador"));
        when(fundo.getId()).thenReturn(7L);
        usuario.getFundos().add(fundo);
        when(authorizationService.ehAdministrador(authentication)).thenReturn(false);
        when(authorizationService.usuarioAtual(authentication)).thenReturn(usuario);
        when(repository.buscarExtrato(any(), isNull(), isNull(), isNull(), isNull(), eq(false), any()))
                .thenReturn(List.of());

        service.listar(LocalDate.of(2026, 2, 1), null,
                null, null, null, authentication);

        verify(repository).buscarExtrato(
                eq(LocalDate.of(2026, 2, 1).atStartOfDay()),
                isNull(), isNull(), isNull(), isNull(), eq(false), eq(List.of(7L)));
    }

    @Test
    void rejeitaPeriodoInvertido() {
        assertThatThrownBy(() -> service.listar(
                LocalDate.of(2026, 2, 2),
                LocalDate.of(2026, 2, 1),
                null, null, null, authentication))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(authorizationService, repository);
    }
}
