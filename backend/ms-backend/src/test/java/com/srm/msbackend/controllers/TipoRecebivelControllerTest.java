package com.srm.msbackend.controllers;

import com.srm.msbackend.models.TipoRecebivelModel;
import com.srm.msbackend.services.TipoRecebivelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class TipoRecebivelControllerTest {
    @Mock TipoRecebivelService service;
    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = standaloneSetup(new TipoRecebivelController(service)).build();
    }

    @Test
    void listaTipos() throws Exception {
        when(service.listar()).thenReturn(List.of(new TipoRecebivelModel(1L, "Duplicata", BigDecimal.ONE)));
        mvc.perform(get("/api/tipos-recebiveis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Duplicata"));
    }
}
