package com.srm.msbackend.controllers;

import com.srm.msbackend.models.TipoRecebivelModel;
import com.srm.msbackend.services.TipoRecebivelService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-recebiveis")
@Tag(name = "Tipos de recebível", description = "Consulta de tipos de recebível")
@SecurityRequirement(name = "bearerAuth")
public class TipoRecebivelController {
    private final TipoRecebivelService tipoRecebivelService;

    public TipoRecebivelController(TipoRecebivelService tipoRecebivelService) {
        this.tipoRecebivelService = tipoRecebivelService;
    }

    @GetMapping
    @PreAuthorize("@fundoAuthorizationService.temScope(authentication, 'tipos-recebiveis:read')")
    public List<TipoRecebivelModel> listar() {
        return tipoRecebivelService.listar();
    }
}
