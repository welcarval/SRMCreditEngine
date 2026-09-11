package com.srm.msbackend.controllers;

import com.srm.msbackend.models.ScopeModel;
import com.srm.msbackend.repositories.ScopeRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/scopes")
public class ScopeController {
    private final ScopeRepository scopeRepository;

    public ScopeController(ScopeRepository scopeRepository) {
        this.scopeRepository = scopeRepository;
    }

    @GetMapping
    @PreAuthorize("@fundoAuthorizationService.ehAdministrador(authentication)")
    public List<ScopeModel> listar() {
        return scopeRepository.findAll().stream()
                .map(scope -> new ScopeModel(scope.getId(), scope.getCodigo(), scope.getDescricao()))
                .sorted(java.util.Comparator.comparing(ScopeModel::codigo))
                .toList();
    }
}
