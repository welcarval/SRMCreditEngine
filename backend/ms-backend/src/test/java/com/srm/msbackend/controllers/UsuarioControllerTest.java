package com.srm.msbackend.controllers;

import com.srm.msbackend.entities.TipoUsuario;
import com.srm.msbackend.entities.Usuario;
import com.srm.msbackend.models.UsuarioAcessoModel;
import com.srm.msbackend.services.FundoAuthorizationService;
import com.srm.msbackend.services.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {
    @Mock UsuarioService service;
    @Mock FundoAuthorizationService authorizationService;
    @Mock Authentication authentication;
    MockMvc mvc;
    private final Usuario usuario = new Usuario("U", "u@u", new TipoUsuario("ADMIN", "Admin"));
    private final UsuarioAcessoModel acesso = new UsuarioAcessoModel(1L, "U", "u@u", "ADMIN", List.of(1L));

    @BeforeEach
    void setUp() {
        mvc = standaloneSetup(new UsuarioController(service, authorizationService)).build();
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder auth(
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request) {
        return request.with(authentication(authentication));
    }

    @Test
    void executaCrudEMe() throws Exception {
        when(authorizationService.usuarioAtual(nullable(Authentication.class))).thenReturn(usuario);
        when(authorizationService.ehAdministrador(nullable(Authentication.class))).thenReturn(true);
        when(service.toAcessoModel(usuario)).thenReturn(acesso);
        when(service.listar()).thenReturn(List.of(acesso));
        when(service.buscarPorId(1L)).thenReturn(Optional.of(usuario));
        when(service.buscarPorId(9L)).thenReturn(Optional.empty());
        when(service.salvar(any())).thenReturn(usuario);
        when(service.atualizar(eq(1L), any())).thenReturn(Optional.of(usuario));
        when(service.atualizar(eq(9L), any())).thenReturn(Optional.empty());
        when(service.deletar(1L)).thenReturn(true);
        when(service.deletar(9L)).thenReturn(false);

        mvc.perform(auth(get("/api/usuarios/me"))).andExpect(status().isOk());
        mvc.perform(auth(get("/api/usuarios"))).andExpect(status().isOk());
        mvc.perform(auth(get("/api/usuarios/1"))).andExpect(status().isOk());
        mvc.perform(auth(get("/api/usuarios/9"))).andExpect(status().isNotFound());
        mvc.perform(auth(post("/api/usuarios").contentType("application/json").content("{}")))
                .andExpect(status().isOk());
        mvc.perform(auth(put("/api/usuarios/1").contentType("application/json").content("{}")))
                .andExpect(status().isOk());
        mvc.perform(auth(put("/api/usuarios/9").contentType("application/json").content("{}")))
                .andExpect(status().isNotFound());
        mvc.perform(auth(delete("/api/usuarios/1"))).andExpect(status().isNoContent());
        mvc.perform(auth(delete("/api/usuarios/9"))).andExpect(status().isNotFound());
    }

    @Test
    void bloqueiaOperador() {
        when(authorizationService.ehAdministrador(authentication)).thenReturn(false);
        UsuarioController controller = new UsuarioController(service, authorizationService);
        assertThatThrownBy(() -> controller.deletar(1L, authentication))
                .isInstanceOf(AccessDeniedException.class);
    }
}
