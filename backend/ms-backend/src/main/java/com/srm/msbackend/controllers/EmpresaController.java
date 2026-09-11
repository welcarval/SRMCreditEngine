package com.srm.msbackend.controllers;

import com.srm.msbackend.models.EmpresaModel;
import com.srm.msbackend.services.EmpresaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresas")
@Tag(name = "Empresas", description = "Gerenciamento de empresas")
@SecurityRequirement(name = "bearerAuth")
public class EmpresaController {
    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @GetMapping
    @PreAuthorize("@fundoAuthorizationService.temScope(authentication, 'empresas:read')")
    public List<EmpresaModel> listar() {
        return empresaService.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("@fundoAuthorizationService.temScope(authentication, 'empresas:read')")
    public ResponseEntity<EmpresaModel> buscarPorId(@PathVariable Long id) {
        return empresaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("@fundoAuthorizationService.temScope(authentication, 'empresas:write')")
    public EmpresaModel criar(@RequestBody EmpresaModel model) {
        return empresaService.salvar(model);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@fundoAuthorizationService.temScope(authentication, 'empresas:write')")
    public ResponseEntity<EmpresaModel> atualizar(@PathVariable Long id, @RequestBody EmpresaModel model) {
        return empresaService.atualizar(id, model)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@fundoAuthorizationService.temScope(authentication, 'empresas:write')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        return empresaService.deletar(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
