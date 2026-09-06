package com.srm.msbackend.services;

import com.srm.msbackend.entities.Conta;
import com.srm.msbackend.entities.Fundo;
import com.srm.msbackend.models.FundoModel;
import com.srm.msbackend.repositories.ContaRepository;
import com.srm.msbackend.repositories.FundoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FundoService {
    private final FundoRepository fundoRepository;
    private final ContaRepository contaRepository;

    public FundoService(FundoRepository fundoRepository, ContaRepository contaRepository) {
        this.fundoRepository = fundoRepository;
        this.contaRepository = contaRepository;
    }

    public List<Fundo> listar() {
        return fundoRepository.findAll();
    }

    public Optional<Fundo> buscarPorId(Long id) {
        return fundoRepository.findById(id);
    }

    public Fundo salvar(FundoModel model) {
        Fundo fundo = new Fundo(model.nome(), model.cnpj(), model.taxaBase(), null);

        if (model.contaId() != null) {
            Conta conta = contaRepository.findById(model.contaId())
                    .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada: " + model.contaId()));
            fundo.setConta(conta);
        }

        return fundoRepository.save(fundo);
    }

    public Optional<Fundo> atualizar(Long id, FundoModel model) {
        return fundoRepository.findById(id).map(fundo -> {
            fundo.setNome(model.nome());
            fundo.setCnpj(model.cnpj());
            fundo.setTaxaBase(model.taxaBase());

            if (model.contaId() != null) {
                Conta conta = contaRepository.findById(model.contaId())
                        .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada: " + model.contaId()));
                fundo.setConta(conta);
            }

            return fundoRepository.save(fundo);
        });
    }

    public boolean deletar(Long id) {
        if (!fundoRepository.existsById(id)) {
            return false;
        }

        fundoRepository.deleteById(id);
        return true;
    }
}
