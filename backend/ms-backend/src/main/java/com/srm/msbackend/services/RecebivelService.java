package com.srm.msbackend.services;

import com.srm.msbackend.entities.Empresa;
import com.srm.msbackend.entities.Fundo;
import com.srm.msbackend.entities.Recebivel;
import com.srm.msbackend.entities.TipoRecebivel;
import com.srm.msbackend.models.RecebivelModel;
import com.srm.msbackend.repositories.EmpresaRepository;
import com.srm.msbackend.repositories.FundoRepository;
import com.srm.msbackend.repositories.RecebivelRepository;
import com.srm.msbackend.repositories.TipoRecebivelRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class RecebivelService {
    private final RecebivelRepository recebivelRepository;
    private final FundoRepository fundoRepository;
    private final EmpresaRepository empresaRepository;
    private final TipoRecebivelRepository tipoRecebivelRepository;

    public RecebivelService(RecebivelRepository recebivelRepository,
                            FundoRepository fundoRepository,
                            EmpresaRepository empresaRepository,
                            TipoRecebivelRepository tipoRecebivelRepository) {
        this.recebivelRepository = recebivelRepository;
        this.fundoRepository = fundoRepository;
        this.empresaRepository = empresaRepository;
        this.tipoRecebivelRepository = tipoRecebivelRepository;
    }

    public List<Recebivel> listar() {
        return recebivelRepository.findAll();
    }

    public Optional<Recebivel> buscarPorId(Long id) {
        return recebivelRepository.findById(id);
    }

    public Recebivel salvar(RecebivelModel model) {
        Fundo fundo = fundoRepository.findById(model.fundoId())
                .orElseThrow(() -> new IllegalArgumentException("Fundo não encontrado: " + model.fundoId()));

        Empresa empresa = empresaRepository.findById(model.empresaId())
                .orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada: " + model.empresaId()));

        TipoRecebivel tipo = tipoRecebivelRepository.findById(model.tipoId())
                .orElseThrow(() -> new IllegalArgumentException("Tipo de recebível não encontrado: " + model.tipoId()));

        Recebivel recebivel = new Recebivel();
        recebivel.setValorFace(model.valorFace());
        recebivel.setDataVencimento(model.dataVencimento());
        recebivel.setFundo(fundo);
        recebivel.setEmpresa(empresa);
        recebivel.setTipo(tipo);

        recebivel.setValorPresente(calcularValorPresente(
                model.valorFace(),
                fundo.getTaxaBase(),
                tipo.getSpread(),
                calcularPrazoEmAnos(model.dataVencimento())
        ));
        return recebivelRepository.save(recebivel);
    }

    public Optional<Recebivel> atualizar(Long id, RecebivelModel model) {
        return recebivelRepository.findById(id).map(recebivel -> {
            Fundo fundo = fundoRepository.findById(model.fundoId())
                    .orElseThrow(() -> new IllegalArgumentException("Fundo não encontrado: " + model.fundoId()));

            Empresa empresa = empresaRepository.findById(model.empresaId())
                    .orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada: " + model.empresaId()));

            TipoRecebivel tipo = tipoRecebivelRepository.findById(model.tipoId())
                    .orElseThrow(() -> new IllegalArgumentException("Tipo de recebível não encontrado: " + model.tipoId()));

            recebivel.setValorFace(model.valorFace());
            recebivel.setDataVencimento(model.dataVencimento());
            recebivel.setFundo(fundo);
            recebivel.setEmpresa(empresa);
            recebivel.setTipo(tipo);

            recebivel.setValorPresente(calcularValorPresente(
                    model.valorFace(),
                    fundo.getTaxaBase(),
                    tipo.getSpread(),
                    calcularPrazoEmAnos(model.dataVencimento())
            ));
            return recebivelRepository.save(recebivel);
        });
    }

    public boolean deletar(Long id) {
        if (!recebivelRepository.existsById(id)) {
            return false;
        }

        recebivelRepository.deleteById(id);
        return true;
    }

    public BigDecimal calcularValorPresente(BigDecimal valorFace, BigDecimal taxaBase, BigDecimal spread, BigDecimal prazo) {
        if (valorFace == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal taxaTotal = taxaBase == null ? BigDecimal.ZERO : taxaBase;
        if (spread != null) {
            taxaTotal = taxaTotal.add(spread);
        }

        BigDecimal divisor = BigDecimal.ONE.add(taxaTotal);
        double expoente = prazo == null ? 1d : prazo.doubleValue();
        double denominador = Math.pow(divisor.doubleValue(), expoente);

        return valorFace.divide(BigDecimal.valueOf(denominador), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal calcularPrazoEmAnos(LocalDate dataVencimento) {
        if (dataVencimento == null) {
            return BigDecimal.ONE;
        }

        long dias = ChronoUnit.DAYS.between(LocalDate.now(), dataVencimento);
        return BigDecimal.valueOf(dias)
                .divide(BigDecimal.valueOf(365), 10, RoundingMode.HALF_UP)
                .max(BigDecimal.ZERO);
    }
}
