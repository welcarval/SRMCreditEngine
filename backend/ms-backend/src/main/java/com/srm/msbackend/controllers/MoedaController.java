package com.srm.msbackend.controllers;

import com.srm.msbackend.models.MoedaModel;
import com.srm.msbackend.services.MoedaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/moedas")
public class MoedaController {
    private final MoedaService moedaService;

    public MoedaController(MoedaService moedaService) {
        this.moedaService = moedaService;
    }

    @GetMapping
    public List<MoedaModel> listar() {
        return moedaService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MoedaModel> buscarPorId(@PathVariable Long id) {
        return moedaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public MoedaModel criar(@RequestBody MoedaModel model) {
        return moedaService.salvar(model);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MoedaModel> atualizar(@PathVariable Long id, @RequestBody MoedaModel model) {
        return moedaService.atualizar(id, model)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        return moedaService.deletar(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
