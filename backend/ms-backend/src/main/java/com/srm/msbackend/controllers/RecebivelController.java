package com.srm.msbackend.controllers;

import com.srm.msbackend.models.RecebivelModel;
import com.srm.msbackend.services.RecebivelService;
import com.srm.msbackend.services.FundoAuthorizationService;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recebiveis")
public class RecebivelController {
    private final RecebivelService recebivelService;
    private final FundoAuthorizationService authorizationService;

    public RecebivelController(RecebivelService recebivelService,
                               FundoAuthorizationService authorizationService) {
        this.recebivelService = recebivelService;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    public List<RecebivelModel> listar(Authentication authentication) {
        return authorizationService.ehAdministrador(authentication)
                ? recebivelService.listar()
                : recebivelService.listarParaUsuario(authorizationService.usuarioAtual(authentication).getId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecebivelModel> buscarPorId(@PathVariable Long id, Authentication authentication) {
        exigirAdministrador(authentication);
        return recebivelService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public RecebivelModel criar(@RequestBody RecebivelModel model, Authentication authentication) {
        exigirAdministrador(authentication);
        return recebivelService.salvar(model);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecebivelModel> atualizar(@PathVariable Long id, @RequestBody RecebivelModel model,
                                                    Authentication authentication) {
        exigirAdministrador(authentication);
        return recebivelService.atualizar(id, model)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id, Authentication authentication) {
        exigirAdministrador(authentication);
        return recebivelService.deletar(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    private void exigirAdministrador(Authentication authentication) {
        if (!authorizationService.ehAdministrador(authentication)) {
            throw new AccessDeniedException("Apenas administradores podem executar esta operação");
        }
    }
}
