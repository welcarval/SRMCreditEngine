package com.srm.msbackend.controllers;

import com.srm.msbackend.models.ExtratoTransacaoModel;
import com.srm.msbackend.entities.StatusTransacao;
import com.srm.msbackend.services.ExtratoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class ExtratoControllerTest {
    @Mock ExtratoService service;
    @Mock Authentication authentication;
    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = standaloneSetup(new ExtratoController(service)).build();
    }

    @Test
    void listaComFiltros() throws Exception {
        when(service.listar(
                eq(LocalDate.of(2026, 1, 1)),
                eq(LocalDate.of(2026, 1, 31)),
                eq(1L), eq(2L), eq(3L), nullable(Authentication.class)))
                .thenReturn(List.of(new ExtratoTransacaoModel(
                        1L, null, null, StatusTransacao.SUCESSO, null, null)));

        mvc.perform(get("/api/extratos")
                        .param("dataInicial", "2026-01-01")
                        .param("dataFinal", "2026-01-31")
                        .param("fundoId", "1")
                        .param("empresaId", "2")
                        .param("moedaId", "3")
                        .with(authentication(authentication)))
                .andExpect(status().isOk());
    }

    @Test
    void geraCsvEParquet() throws Exception {
        when(service.listar(any(), any(), any(), any(), any(), nullable(Authentication.class)))
                        .thenReturn(List.of(new ExtratoTransacaoModel(
                                1L, null, null, StatusTransacao.SUCESSO, null, null)));

        mvc.perform(get("/api/extratos").param("formato", "csv"))
                        .andExpect(status().isOk())
                        .andExpect(header().string("Content-Type", "text/csv"))
                        .andExpect(header().string("Content-Disposition", "attachment; filename=\"extrato.csv\""));

        mvc.perform(get("/api/extratos").param("formato", "parquet"))
                        .andExpect(status().isOk())
                        .andExpect(header().string("Content-Type", "application/vnd.apache.parquet"))
                        .andExpect(header().string("Content-Disposition", "attachment; filename=\"extrato.parquet\""));
    }

    @Test
    void rejeitaFormatoInvalido() throws Exception {
        when(service.listar(any(), any(), any(), any(), any(), nullable(Authentication.class)))
                        .thenReturn(List.of());

        mvc.perform(get("/api/extratos").param("formato", "xml"))
                        .andExpect(status().isBadRequest());
    }
}
