package com.srm.msbackend.services;

import com.srm.msbackend.entities.Moeda;
import com.srm.msbackend.models.MoedaModel;
import com.srm.msbackend.repositories.MoedaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MoedaService {
    private final MoedaRepository moedaRepository;

    public MoedaService(MoedaRepository moedaRepository) {
        this.moedaRepository = moedaRepository;
    }

    public List<Moeda> listar() {
        return moedaRepository.findAll();
    }

    public Optional<Moeda> buscarPorId(Long id) {
        return moedaRepository.findById(id);
    }

    public Moeda salvar(MoedaModel model) {
        return moedaRepository.save(new Moeda(
                model.codigo(),
                model.nome(),
                model.taxaCambioDolar()
        ));
    }

    public Optional<Moeda> atualizar(Long id, MoedaModel model) {
        return moedaRepository.findById(id).map(moeda -> {
            moeda.setCodigo(model.codigo());
            moeda.setNome(model.nome());
            moeda.setTaxaCambioDolar(model.taxaCambioDolar());
            return moedaRepository.save(moeda);
        });
    }

    public boolean deletar(Long id) {
        if (!moedaRepository.existsById(id)) {
            return false;
        }

        moedaRepository.deleteById(id);
        return true;
    }
}
