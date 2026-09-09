package com.srm.msbackend.services;

import com.srm.msbackend.entities.Moeda;
import com.srm.msbackend.models.MoedaModel;
import com.srm.msbackend.repositories.MoedaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MoedaService {
    private final MoedaRepository moedaRepository;

    public MoedaService(MoedaRepository moedaRepository) {
        this.moedaRepository = moedaRepository;
    }

    @Transactional(readOnly = true)
    public List<MoedaModel> listar() {
        return moedaRepository.findAll().stream().map(this::toModel).toList();
    }

    @Transactional(readOnly = true)
    public Optional<MoedaModel> buscarPorId(Long id) {
        return moedaRepository.findById(id).map(this::toModel);
    }

    @Transactional
    public MoedaModel salvar(MoedaModel model) {
        return toModel(moedaRepository.save(new Moeda(
                model.codigo(),
                model.nome(),
                model.taxaCambioDolar()
        )));
    }

    @Transactional
    public Optional<MoedaModel> atualizar(Long id, MoedaModel model) {
        return moedaRepository.findById(id).map(moeda -> {
            moeda.setCodigo(model.codigo());
            moeda.setNome(model.nome());
            moeda.setTaxaCambioDolar(model.taxaCambioDolar());
            return toModel(moedaRepository.save(moeda));
        });
    }

    @Transactional
    public boolean deletar(Long id) {
        if (!moedaRepository.existsById(id)) {
            return false;
        }

        moedaRepository.deleteById(id);
        return true;
    }

    private MoedaModel toModel(Moeda moeda) {
        return new MoedaModel(
                moeda.getId(),
                moeda.getCodigo(),
                moeda.getNome(),
                moeda.getTaxaCambioDolar()
        );
    }
}
