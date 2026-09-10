package com.srm.msbackend.controllers;

import com.srm.msbackend.models.MoedaModel;
import com.srm.msbackend.services.MoedaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class MoedaControllerTest {
    @Mock MoedaService service;
    MockMvc mvc;
    private final MoedaModel model = new MoedaModel(1L, "BRL", "Real", BigDecimal.ONE);

    @BeforeEach
    void setUp() {
        mvc = standaloneSetup(new MoedaController(service)).build();
    }

    @Test
    void executaCrud() throws Exception {
        when(service.listar()).thenReturn(List.of(model));
        when(service.buscarPorId(1L)).thenReturn(Optional.of(model));
        when(service.buscarPorId(9L)).thenReturn(Optional.empty());
        when(service.salvar(any())).thenReturn(model);
        when(service.atualizar(eq(1L), any())).thenReturn(Optional.of(model));
        when(service.atualizar(eq(9L), any())).thenReturn(Optional.empty());
        when(service.deletar(1L)).thenReturn(true);
        when(service.deletar(9L)).thenReturn(false);

        mvc.perform(get("/api/moedas")).andExpect(status().isOk());
        mvc.perform(get("/api/moedas/1")).andExpect(status().isOk());
        mvc.perform(get("/api/moedas/9")).andExpect(status().isNotFound());
        mvc.perform(post("/api/moedas").contentType("application/json").content("{}")).andExpect(status().isOk());
        mvc.perform(put("/api/moedas/1").contentType("application/json").content("{}")).andExpect(status().isOk());
        mvc.perform(put("/api/moedas/9").contentType("application/json").content("{}")).andExpect(status().isNotFound());
        mvc.perform(delete("/api/moedas/1")).andExpect(status().isNoContent());
        mvc.perform(delete("/api/moedas/9")).andExpect(status().isNotFound());
    }
}
