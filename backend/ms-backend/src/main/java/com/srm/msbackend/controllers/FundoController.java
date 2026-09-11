package com.srm.msbackend.controllers;

import com.srm.msbackend.models.FundoModel;
import com.srm.msbackend.models.RecebivelModel;
import com.srm.msbackend.services.FundoService;
import com.srm.msbackend.services.FundoAuthorizationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fundos")
@Tag(name = "Fundos", description = "Gerenciamento de fundos e associação de usuários")
@SecurityRequirement(name = "bearerAuth")
public class FundoController {
    private final FundoService fundoService;
    private final FundoAuthorizationService authorizationService;

    public FundoController(FundoService fundoService, FundoAuthorizationService authorizationService) {
        this.fundoService = fundoService;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    @PreAuthorize("@fundoAuthorizationService.temScope(authentication, 'fundos:read')")
    public List<FundoModel> listar(Authentication authentication) {
        return authorizationService.ehAdministrador(authentication)
                ? fundoService.listar()
                : fundoService.listarPorUsuario(authorizationService.extrairIdentificador(authentication));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@fundoAuthorizationService.temScopeEAcessoFundo(authentication, 'fundos:read', #p0)")
    public ResponseEntity<FundoModel> buscarPorId(@PathVariable Long id, Authentication authentication) {
        return fundoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("@fundoAuthorizationService.temScope(authentication, 'fundos:write')")
    public FundoModel criar(@RequestBody FundoModel model, Authentication authentication) {
        return fundoService.salvar(model);
    }

    @PostMapping("/{fundoId}/recebiveis/{recebivelId}")
    @PreAuthorize("@fundoAuthorizationService.temScopeEAcessoFundo(authentication, 'fundos:write', #p0)")
    public RecebivelModel adicionarRecebivel(
            @PathVariable Long fundoId,
            @PathVariable Long recebivelId,
            Authentication authentication) {
        return fundoService.adicionarRecebivel(fundoId, recebivelId);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@fundoAuthorizationService.temScope(authentication, 'fundos:write')")
    public ResponseEntity<FundoModel> atualizar(
            @PathVariable Long id,
            @RequestBody FundoModel model,
            Authentication authentication) {
        return fundoService.atualizar(id, model)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@fundoAuthorizationService.temScope(authentication, 'fundos:write')")
    public ResponseEntity<Void> deletar(@PathVariable Long id, Authentication authentication) {
        return fundoService.deletar(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PutMapping("/{fundoId}/usuarios/{usuarioId}")
    @PreAuthorize("@fundoAuthorizationService.temScope(authentication, 'fundos:write')")
    public ResponseEntity<Void> associarUsuario(
            @PathVariable Long fundoId,
            @PathVariable Long usuarioId,
            Authentication authentication) {
        fundoService.associarUsuario(fundoId, usuarioId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{fundoId}/usuarios/{usuarioId}")
    @PreAuthorize("@fundoAuthorizationService.temScope(authentication, 'fundos:write')")
    public ResponseEntity<Void> removerUsuario(
            @PathVariable Long fundoId,
            @PathVariable Long usuarioId,
            Authentication authentication) {
        fundoService.removerUsuario(fundoId, usuarioId);
        return ResponseEntity.noContent().build();
    }
}
