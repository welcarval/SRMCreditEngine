package com.srm.msbackend.controllers;

import com.srm.msbackend.models.FundoModel;
import com.srm.msbackend.models.RecebivelModel;
import com.srm.msbackend.services.FundoAuthorizationService;
import com.srm.msbackend.services.FundoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class FundoControllerTest {
    @Mock FundoService service;
    @Mock FundoAuthorizationService authorizationService;
    @Mock Authentication authentication;
    MockMvc mvc;
    private final FundoModel fundo = new FundoModel(1L, "Fundo", "1", BigDecimal.TEN, 2L, BigDecimal.ONE);
    private final RecebivelModel recebivel = new RecebivelModel(
            1L, BigDecimal.TEN, BigDecimal.ONE, null, null, 2L, 3L,
            BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE);

    @BeforeEach
    void setUp() {
        mvc = standaloneSetup(new FundoController(service, authorizationService)).build();
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder auth(
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request) {
        return request.with(authentication(authentication));
    }

    @Test
    void executaRotasComoAdminEOperador() throws Exception {
        when(authorizationService.ehAdministrador(nullable(Authentication.class))).thenReturn(true);
        when(service.listar()).thenReturn(List.of(fundo));
        when(service.buscarPorId(1L)).thenReturn(Optional.of(fundo));
        when(service.salvar(any())).thenReturn(fundo);
        when(service.adicionarRecebivel(1L, 2L)).thenReturn(recebivel);
        when(service.atualizar(eq(1L), any())).thenReturn(Optional.of(fundo));
        when(service.deletar(1L)).thenReturn(true);

        mvc.perform(auth(get("/api/fundos"))).andExpect(status().isOk());
        mvc.perform(auth(get("/api/fundos/1"))).andExpect(status().isOk());
        mvc.perform(auth(post("/api/fundos").contentType("application/json").content("{}")))
                .andExpect(status().isOk());
        mvc.perform(auth(post("/api/fundos/1/recebiveis/2"))).andExpect(status().isOk());
        mvc.perform(auth(put("/api/fundos/1").contentType("application/json").content("{}")))
                .andExpect(status().isOk());
        mvc.perform(auth(delete("/api/fundos/1"))).andExpect(status().isNoContent());
        mvc.perform(auth(put("/api/fundos/1/usuarios/2"))).andExpect(status().isNoContent());
        mvc.perform(auth(delete("/api/fundos/1/usuarios/2"))).andExpect(status().isNoContent());

        when(authorizationService.ehAdministrador(nullable(Authentication.class))).thenReturn(false);
        when(authorizationService.extrairIdentificador(nullable(Authentication.class))).thenReturn("u@u");
        when(service.listarPorUsuario("u@u")).thenReturn(List.of(fundo));
        mvc.perform(auth(get("/api/fundos"))).andExpect(status().isOk());
    }
}
