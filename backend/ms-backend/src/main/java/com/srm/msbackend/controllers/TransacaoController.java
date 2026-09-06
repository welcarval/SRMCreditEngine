package com.srm.msbackend.controllers;

import com.srm.msbackend.entities.Transacao;
import com.srm.msbackend.models.TransacaoModel;
import com.srm.msbackend.services.TransacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transacoes")
public class TransacaoController {
    private final TransacaoService transacaoService;

    public TransacaoController(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    @GetMapping
    public List<Transacao> listar() {
        return transacaoService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transacao> buscarPorId(@PathVariable Long id) {
        return transacaoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Transacao criar(@RequestBody TransacaoModel model) {
        return transacaoService.salvar(model);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        return transacaoService.deletar(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
