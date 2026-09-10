package com.srm.msbackend.controllers;

import com.srm.msbackend.entities.Usuario;
import com.srm.msbackend.models.UsuarioModel;
import com.srm.msbackend.services.UsuarioService;
import com.srm.msbackend.services.FundoAuthorizationService;
import com.srm.msbackend.models.UsuarioAcessoModel;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioService usuarioService;
    private final FundoAuthorizationService authorizationService;

    public UsuarioController(UsuarioService usuarioService, FundoAuthorizationService authorizationService) {
        this.usuarioService = usuarioService;
        this.authorizationService = authorizationService;
    }

    @GetMapping("/me")
    public UsuarioAcessoModel me(Authentication authentication) {
        return usuarioService.toAcessoModel(authorizationService.usuarioAtual(authentication));
    }

    @GetMapping
    public List<UsuarioAcessoModel> listar(Authentication authentication) {
        exigirAdministrador(authentication);
        return usuarioService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable Long id, Authentication authentication) {
        exigirAdministrador(authentication);
        return usuarioService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Usuario criar(@RequestBody UsuarioModel model, Authentication authentication) {
        exigirAdministrador(authentication);
        return usuarioService.salvar(model);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> atualizar(@PathVariable Long id, @RequestBody UsuarioModel model,
                                             Authentication authentication) {
        exigirAdministrador(authentication);
        return usuarioService.atualizar(id, model)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id, Authentication authentication) {
        exigirAdministrador(authentication);
        return usuarioService.deletar(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    private void exigirAdministrador(Authentication authentication) {
        if (!authorizationService.ehAdministrador(authentication)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Apenas administradores podem executar esta operação");
        }
    }
}
