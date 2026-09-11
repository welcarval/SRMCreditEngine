package com.srm.msbackend.controllers;

import com.srm.msbackend.models.MoedaModel;
import com.srm.msbackend.services.MoedaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/moedas")
@Tag(name = "Moedas", description = "Gerenciamento de moedas")
@SecurityRequirement(name = "bearerAuth")
public class MoedaController {
    private final MoedaService moedaService;

    public MoedaController(MoedaService moedaService) {
        this.moedaService = moedaService;
    }

    @GetMapping
    @PreAuthorize("@fundoAuthorizationService.temScope(authentication, 'moedas:read')")
    public List<MoedaModel> listar() {
        return moedaService.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("@fundoAuthorizationService.temScope(authentication, 'moedas:read')")
    public ResponseEntity<MoedaModel> buscarPorId(@PathVariable Long id) {
        return moedaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("@fundoAuthorizationService.temScope(authentication, 'moedas:write')")
    public MoedaModel criar(@RequestBody MoedaModel model) {
        return moedaService.salvar(model);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@fundoAuthorizationService.temScope(authentication, 'moedas:write')")
    public ResponseEntity<MoedaModel> atualizar(@PathVariable Long id, @RequestBody MoedaModel model) {
        return moedaService.atualizar(id, model)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@fundoAuthorizationService.temScope(authentication, 'moedas:write')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        return moedaService.deletar(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
