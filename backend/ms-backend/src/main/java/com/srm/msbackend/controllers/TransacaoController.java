package com.srm.msbackend.controllers;

import com.srm.msbackend.entities.Transacao;
import com.srm.msbackend.models.TransacaoModel;
import com.srm.msbackend.services.TransacaoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transacoes")
@Tag(name = "Transações", description = "Gerenciamento de transações")
@SecurityRequirement(name = "bearerAuth")
public class TransacaoController {
    private final TransacaoService transacaoService;

    public TransacaoController(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    @GetMapping
    @PreAuthorize("@fundoAuthorizationService.temScope(authentication, 'transacoes:read')")
    public List<Transacao> listar() {
        return transacaoService.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("@fundoAuthorizationService.temScope(authentication, 'transacoes:read')")
    public ResponseEntity<Transacao> buscarPorId(@PathVariable Long id) {
        return transacaoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("@fundoAuthorizationService.temScope(authentication, 'transacoes:write')")
    public Transacao criar(@RequestBody TransacaoModel model) {
        return transacaoService.salvar(model);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@fundoAuthorizationService.temScope(authentication, 'transacoes:write')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        return transacaoService.deletar(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
