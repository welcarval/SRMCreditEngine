package com.srm.msbackend.controllers;

import com.srm.msbackend.models.EmpresaModel;
import com.srm.msbackend.services.EmpresaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class EmpresaControllerTest {
    @Mock EmpresaService service;
    MockMvc mvc;
    private final EmpresaModel model = new EmpresaModel(1L, "Empresa", "1", 2L);

    @BeforeEach
    void setUp() {
        mvc = standaloneSetup(new EmpresaController(service)).build();
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

        mvc.perform(get("/api/empresas")).andExpect(status().isOk());
        mvc.perform(get("/api/empresas/1")).andExpect(status().isOk());
        mvc.perform(get("/api/empresas/9")).andExpect(status().isNotFound());
        mvc.perform(post("/api/empresas").contentType("application/json").content("{}")).andExpect(status().isOk());
        mvc.perform(put("/api/empresas/1").contentType("application/json").content("{}")).andExpect(status().isOk());
        mvc.perform(put("/api/empresas/9").contentType("application/json").content("{}")).andExpect(status().isNotFound());
        mvc.perform(delete("/api/empresas/1")).andExpect(status().isNoContent());
        mvc.perform(delete("/api/empresas/9")).andExpect(status().isNotFound());
    }
}
