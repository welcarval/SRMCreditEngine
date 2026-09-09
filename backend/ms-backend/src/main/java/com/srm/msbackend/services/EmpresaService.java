package com.srm.msbackend.services;

import com.srm.msbackend.entities.Conta;
import com.srm.msbackend.entities.Empresa;
import com.srm.msbackend.models.EmpresaModel;
import com.srm.msbackend.repositories.ContaRepository;
import com.srm.msbackend.repositories.EmpresaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
    public List<EmpresaModel> listar() {
        return empresaRepository.findAll().stream().map(this::toModel).toList();
    }

    @Transactional(readOnly = true)
    public Optional<EmpresaModel> buscarPorId(Long id) {
        return empresaRepository.findById(id).map(this::toModel);
    }

    @Transactional
    public EmpresaModel salvar(EmpresaModel model) {
        Conta conta = buscarConta(model.contaId());
        return toModel(empresaRepository.save(new Empresa(model.razaoSocial(), model.cnpj(), conta)));
    }

    @Transactional
    public Optional<EmpresaModel> atualizar(Long id, EmpresaModel model) {
        return empresaRepository.findById(id).map(empresa -> {
            empresa.setRazaoSocial(model.razaoSocial());
            empresa.setCnpj(model.cnpj());
            empresa.setConta(buscarConta(model.contaId()));
            return toModel(empresaRepository.save(empresa));
        });
    }

    @Transactional
    public boolean deletar(Long id) {
        if (!empresaRepository.existsById(id)) {
            return false;
        }

        empresaRepository.deleteById(id);
        return true;
    }

    private EmpresaModel toModel(Empresa empresa) {
        return new EmpresaModel(
                empresa.getId(),
                empresa.getRazaoSocial(),
                empresa.getCnpj(),
                empresa.getConta() == null ? null : empresa.getConta().getId()
        );
    }

    private Conta buscarConta(Long contaId) {
        return contaRepository.findById(contaId)
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada: " + contaId));
    }
}
