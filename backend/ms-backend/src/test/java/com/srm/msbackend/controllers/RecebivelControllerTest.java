package com.srm.msbackend.controllers;

import com.srm.msbackend.entities.TipoUsuario;
import com.srm.msbackend.entities.Usuario;
import com.srm.msbackend.models.RecebivelModel;
import com.srm.msbackend.services.FundoAuthorizationService;
import com.srm.msbackend.services.RecebivelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
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
class RecebivelControllerTest {
    @Mock RecebivelService service;
    @Mock FundoAuthorizationService authorizationService;
    @Mock Authentication authentication;
    MockMvc mvc;
    private final RecebivelModel model = new RecebivelModel(
            1L, BigDecimal.TEN, BigDecimal.ONE, LocalDate.now(), null, 2L, 3L,
            BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE);

    @BeforeEach
    void setUp() {
        mvc = standaloneSetup(new RecebivelController(service, authorizationService)).build();
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder auth(
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request) {
        return request.with(authentication(authentication));
    }

    @Test
    void executaRotasComoAdminEOperador() throws Exception {
        when(authorizationService.ehAdministrador(nullable(Authentication.class))).thenReturn(true);
        when(service.listar()).thenReturn(List.of(model));
        when(service.buscarPorId(1L)).thenReturn(Optional.of(model));
        when(service.salvar(any())).thenReturn(model);
        when(service.atualizar(eq(1L), any())).thenReturn(Optional.of(model));
        when(service.deletar(1L)).thenReturn(true);

        mvc.perform(auth(get("/api/recebiveis"))).andExpect(status().isOk());
        mvc.perform(auth(get("/api/recebiveis/1"))).andExpect(status().isOk());
        String payload = "{\"valorFace\":10,\"tipoId\":2,\"empresaId\":3,\"taxaBase\":1}";
        mvc.perform(auth(post("/api/recebiveis").contentType("application/json").content(payload)))
                .andExpect(status().isOk());
        mvc.perform(auth(put("/api/recebiveis/1").contentType("application/json").content(payload)))
                .andExpect(status().isOk());
        mvc.perform(auth(delete("/api/recebiveis/1"))).andExpect(status().isNoContent());

        Usuario operador = new Usuario("U", "u@u", new TipoUsuario("OPERADOR", "Operador"));
        when(authorizationService.ehAdministrador(nullable(Authentication.class))).thenReturn(false);
        when(authorizationService.usuarioAtual(nullable(Authentication.class))).thenReturn(operador);
        when(service.listarParaUsuario(null)).thenReturn(List.of());
        mvc.perform(auth(get("/api/recebiveis"))).andExpect(status().isOk());
        mvc.perform(auth(get("/api/recebiveis/1"))).andExpect(status().isOk());
        mvc.perform(auth(post("/api/recebiveis").contentType("application/json").content(payload)))
                .andExpect(status().isOk());
    }

    @Test
    void bloqueiaOperacoesAdministrativas() {
        when(authorizationService.ehAdministrador(authentication)).thenReturn(false);
        RecebivelController controller = new RecebivelController(service, authorizationService);
        assertThatThrownBy(() -> controller.atualizar(1L, model, authentication))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> controller.deletar(1L, authentication))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void rejeitaTaxaBaseForaDoIntervalo() throws Exception {
        mvc.perform(auth(post("/api/recebiveis")
                        .contentType("application/json")
                        .content("{\"valorFace\":10,\"tipoId\":2,\"empresaId\":3,\"taxaBase\":-1}")))
                .andExpect(status().isBadRequest());
        mvc.perform(auth(post("/api/recebiveis")
                        .contentType("application/json")
                        .content("{\"valorFace\":10,\"tipoId\":2,\"empresaId\":3,\"taxaBase\":101}")))
                .andExpect(status().isBadRequest());
    }
}
