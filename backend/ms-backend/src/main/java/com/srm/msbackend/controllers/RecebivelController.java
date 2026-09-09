package com.srm.msbackend.controllers;

import com.srm.msbackend.models.RecebivelModel;
import com.srm.msbackend.services.RecebivelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recebiveis")
public class RecebivelController {
    private final RecebivelService recebivelService;

    public RecebivelController(RecebivelService recebivelService) {
        this.recebivelService = recebivelService;
    }

    @GetMapping
    public List<RecebivelModel> listar() {
        return recebivelService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecebivelModel> buscarPorId(@PathVariable Long id) {
        return recebivelService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public RecebivelModel criar(@RequestBody RecebivelModel model) {
        return recebivelService.salvar(model);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecebivelModel> atualizar(@PathVariable Long id, @RequestBody RecebivelModel model) {
        return recebivelService.atualizar(id, model)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        return recebivelService.deletar(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
