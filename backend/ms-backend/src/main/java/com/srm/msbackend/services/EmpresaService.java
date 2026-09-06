package com.srm.msbackend.services;

import com.srm.msbackend.entities.Conta;
import com.srm.msbackend.entities.Empresa;
import com.srm.msbackend.models.EmpresaModel;
import com.srm.msbackend.repositories.ContaRepository;
import com.srm.msbackend.repositories.EmpresaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmpresaService {
    private final EmpresaRepository empresaRepository;
    private final ContaRepository contaRepository;

    public EmpresaService(EmpresaRepository empresaRepository, ContaRepository contaRepository) {
        this.empresaRepository = empresaRepository;
        this.contaRepository = contaRepository;
    }

    public List<Empresa> listar() {
        return empresaRepository.findAll();
    }

    public Optional<Empresa> buscarPorId(Long id) {
        return empresaRepository.findById(id);
    }

    public Empresa salvar(EmpresaModel model) {
        Conta conta = buscarConta(model.contaId());
        return empresaRepository.save(new Empresa(model.razaoSocial(), model.cnpj(), conta));
    }

    public Optional<Empresa> atualizar(Long id, EmpresaModel model) {
        return empresaRepository.findById(id).map(empresa -> {
            empresa.setRazaoSocial(model.razaoSocial());
            empresa.setCnpj(model.cnpj());
            empresa.setConta(buscarConta(model.contaId()));
            return empresaRepository.save(empresa);
        });
    }

    public boolean deletar(Long id) {
        if (!empresaRepository.existsById(id)) {
            return false;
        }

        empresaRepository.deleteById(id);
        return true;
    }

    private Conta buscarConta(Long contaId) {
        return contaRepository.findById(contaId)
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada: " + contaId));
    }
}
