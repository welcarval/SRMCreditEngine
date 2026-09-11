package com.srm.msbackend.controllers;

import com.srm.msbackend.models.RecebivelModel;
import com.srm.msbackend.services.RecebivelService;
import com.srm.msbackend.services.FundoAuthorizationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/recebiveis")
@Tag(name = "Recebíveis", description = "Consulta e gerenciamento de recebíveis")
@SecurityRequirement(name = "bearerAuth")
public class RecebivelController {
    private final RecebivelService recebivelService;
    private final FundoAuthorizationService authorizationService;

    public RecebivelController(RecebivelService recebivelService,
                               FundoAuthorizationService authorizationService) {
        this.recebivelService = recebivelService;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    @PreAuthorize("@fundoAuthorizationService.temScope(authentication, 'recebiveis:read')")
    public List<RecebivelModel> listar(Authentication authentication) {
        return authorizationService.ehAdministrador(authentication)
                ? recebivelService.listar()
                : recebivelService.listarParaUsuario(authorizationService.usuarioAtual(authentication).getId());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@fundoAuthorizationService.temScope(authentication, 'recebiveis:read')")
    public ResponseEntity<RecebivelModel> buscarPorId(@PathVariable Long id, Authentication authentication) {
        return recebivelService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("@fundoAuthorizationService.temScope(authentication, 'recebiveis:write')")
    public RecebivelModel criar(@Valid @RequestBody RecebivelModel model, Authentication authentication) {
        return recebivelService.salvar(model);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@fundoAuthorizationService.temScope(authentication, 'recebiveis:write')")
    public ResponseEntity<RecebivelModel> atualizar(@PathVariable Long id, @Valid @RequestBody RecebivelModel model,
                                                    Authentication authentication) {
        return recebivelService.atualizar(id, model)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@fundoAuthorizationService.temScope(authentication, 'recebiveis:write')")
    public ResponseEntity<Void> deletar(@PathVariable Long id, Authentication authentication) {
        return recebivelService.deletar(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
