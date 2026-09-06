package com.srm.msbackend.controllers;

import com.srm.msbackend.entities.Fundo;
import com.srm.msbackend.models.FundoModel;
import com.srm.msbackend.services.FundoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fundos")
public class FundoController {
    private final FundoService fundoService;

    public FundoController(FundoService fundoService) {
        this.fundoService = fundoService;
    }

    @GetMapping
    public List<Fundo> listar() {
        return fundoService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Fundo> buscarPorId(@PathVariable Long id) {
        return fundoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Fundo criar(@RequestBody FundoModel model) {
        return fundoService.salvar(model);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Fundo> atualizar(@PathVariable Long id, @RequestBody FundoModel model) {
        return fundoService.atualizar(id, model)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        return fundoService.deletar(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
