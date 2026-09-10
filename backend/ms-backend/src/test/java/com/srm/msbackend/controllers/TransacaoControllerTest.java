package com.srm.msbackend.controllers;

import com.srm.msbackend.entities.Transacao;
import com.srm.msbackend.services.TransacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class TransacaoControllerTest {
    @Mock TransacaoService service;
    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = standaloneSetup(new TransacaoController(service)).build();
    }

    @Test
    void executaRotas() throws Exception {
        when(service.listar()).thenReturn(List.of());
        when(service.buscarPorId(1L)).thenReturn(Optional.empty());
        when(service.salvar(any())).thenReturn(new Transacao());
        when(service.deletar(1L)).thenReturn(true);
        when(service.deletar(9L)).thenReturn(false);

        mvc.perform(get("/api/transacoes")).andExpect(status().isOk());
        mvc.perform(get("/api/transacoes/1")).andExpect(status().isNotFound());
        mvc.perform(post("/api/transacoes").contentType("application/json").content("{}"))
                .andExpect(status().isOk());
        mvc.perform(delete("/api/transacoes/1")).andExpect(status().isNoContent());
        mvc.perform(delete("/api/transacoes/9")).andExpect(status().isNotFound());
    }
}
