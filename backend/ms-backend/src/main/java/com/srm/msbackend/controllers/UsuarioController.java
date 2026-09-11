package com.srm.msbackend.controllers;

import com.srm.msbackend.entities.Usuario;
import com.srm.msbackend.models.UsuarioModel;
import com.srm.msbackend.services.UsuarioService;
import com.srm.msbackend.services.FundoAuthorizationService;
import com.srm.msbackend.models.UsuarioAcessoModel;
import com.srm.msbackend.models.UsuarioScopesModel;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuários", description = "Gerenciamento de usuários e perfis")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {
    private final UsuarioService usuarioService;
    private final FundoAuthorizationService authorizationService;

    public UsuarioController(UsuarioService usuarioService, FundoAuthorizationService authorizationService) {
        this.usuarioService = usuarioService;
        this.authorizationService = authorizationService;
    }

    @GetMapping("/me")
    @PreAuthorize("@fundoAuthorizationService.temScope(authentication, 'perfil:read')")
    public UsuarioAcessoModel me(Authentication authentication) {
        return usuarioService.toAcessoModel(authorizationService.usuarioAtual(authentication));
    }

    @GetMapping
    @PreAuthorize("@fundoAuthorizationService.temScope(authentication, 'usuarios:read')")
    public List<UsuarioAcessoModel> listar(Authentication authentication) {
        return usuarioService.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("@fundoAuthorizationService.temScope(authentication, 'usuarios:read')")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable Long id, Authentication authentication) {
        return usuarioService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("@fundoAuthorizationService.temScope(authentication, 'usuarios:write')")
    public Usuario criar(@RequestBody UsuarioModel model, Authentication authentication) {
        return usuarioService.salvar(model);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@fundoAuthorizationService.temScope(authentication, 'usuarios:write')")
    public ResponseEntity<Usuario> atualizar(@PathVariable Long id, @RequestBody UsuarioModel model,
                                             Authentication authentication) {
        return usuarioService.atualizar(id, model)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/scopes")
    @PreAuthorize("@fundoAuthorizationService.ehAdministrador(authentication)")
    public ResponseEntity<UsuarioAcessoModel> atualizarScopes(@PathVariable Long id,
                                                               @RequestBody UsuarioScopesModel model) {
        return usuarioService.atualizarScopes(id, model.scopeIds())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@fundoAuthorizationService.temScope(authentication, 'usuarios:write')")
    public ResponseEntity<Void> deletar(@PathVariable Long id, Authentication authentication) {
        return usuarioService.deletar(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
