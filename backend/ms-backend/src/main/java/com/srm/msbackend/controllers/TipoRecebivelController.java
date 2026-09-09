package com.srm.msbackend.controllers;

import com.srm.msbackend.models.TipoRecebivelModel;
import com.srm.msbackend.services.TipoRecebivelService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-recebiveis")
public class TipoRecebivelController {
    private final TipoRecebivelService tipoRecebivelService;

    public TipoRecebivelController(TipoRecebivelService tipoRecebivelService) {
        this.tipoRecebivelService = tipoRecebivelService;
    }

    @GetMapping
    public List<TipoRecebivelModel> listar() {
        return tipoRecebivelService.listar();
    }
}
