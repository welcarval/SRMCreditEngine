package com.srm.msbackend.services;

import com.srm.msbackend.entities.TipoRecebivel;
import com.srm.msbackend.models.TipoRecebivelModel;
import com.srm.msbackend.repositories.TipoRecebivelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TipoRecebivelService {
    private final TipoRecebivelRepository tipoRecebivelRepository;

    public TipoRecebivelService(TipoRecebivelRepository tipoRecebivelRepository) {
        this.tipoRecebivelRepository = tipoRecebivelRepository;
    }

    @Transactional(readOnly = true)
    public List<TipoRecebivelModel> listar() {
        return tipoRecebivelRepository.findAll().stream()
                .map(this::toModel)
                .toList();
    }

    private TipoRecebivelModel toModel(TipoRecebivel tipo) {
        return new TipoRecebivelModel(
                tipo.getId(),
                tipo.getNome(),
                tipo.getSpread()
        );
    }
}
