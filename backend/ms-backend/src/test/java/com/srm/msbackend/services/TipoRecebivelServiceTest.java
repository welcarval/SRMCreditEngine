package com.srm.msbackend.services;

import com.srm.msbackend.entities.TipoRecebivel;
import com.srm.msbackend.repositories.TipoRecebivelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TipoRecebivelServiceTest {
    @Mock TipoRecebivelRepository repository;
    @InjectMocks TipoRecebivelService service;

    @Test void listaTipos() {
        when(repository.findAll()).thenReturn(List.of(new TipoRecebivel("Duplicata", BigDecimal.TEN)));
        assertThat(service.listar()).hasSize(1).first().extracting("nome").isEqualTo("Duplicata");
    }
}
