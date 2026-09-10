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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    @Transactional(readOnly = true)
    public List<RecebivelModel> listar() {
        return recebivelRepository.findAll().stream()
                .map(this::toModel)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RecebivelModel> listarParaUsuario(Long usuarioId) {
        Set<Long> fundosDoUsuario = fundoRepository.findAllByUsuarios_Id(usuarioId).stream()
                .map(Fundo::getId)
                .collect(java.util.stream.Collectors.toSet());
        return recebivelRepository.findAll().stream()
                .filter(recebivel -> recebivel.getFundo() == null
                        || fundosDoUsuario.contains(recebivel.getFundo().getId()))
                .map(this::toModel)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<RecebivelModel> buscarPorId(Long id) {
        return recebivelRepository.findById(id)
                .map(this::toModel);
    }

    @Transactional
    public RecebivelModel salvar(RecebivelModel model) {
        validarTaxaBase(model);
        Fundo fundo = model.fundoId() == null ? null : fundoRepository.findById(model.fundoId())
                .orElseThrow(() -> new IllegalArgumentException("Fundo não encontrado: " + model.fundoId()));

        Empresa empresa = empresaRepository.findById(model.empresaId())
                .orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada: " + model.empresaId()));

        TipoRecebivel tipo = tipoRecebivelRepository.findById(model.tipoId())
                .orElseThrow(() -> new IllegalArgumentException("Tipo de recebível não encontrado: " + model.tipoId()));

        Recebivel recebivel = new Recebivel();
        recebivel.setValorFace(model.valorFace());
        recebivel.setTaxaBase(model.taxaBase());
        recebivel.setDataVencimento(model.dataVencimento());
        recebivel.setFundo(fundo);
        recebivel.setEmpresa(empresa);
        recebivel.setTipo(tipo);

        recebivel.setValorPresente(fundo == null ? null : calcularValorPresente(
                model.valorFace(), model.taxaBase(), tipo.getSpread(), fundo.getTaxaBase(),
                calcularPrazoEmAnos(model.dataVencimento())));
        return toModel(recebivelRepository.save(recebivel));
    }

    @Transactional
    public Optional<RecebivelModel> atualizar(Long id, RecebivelModel model) {
        return recebivelRepository.findById(id).map(recebivel -> {
            validarTaxaBase(model);
            Fundo fundo = model.fundoId() == null ? null : fundoRepository.findById(model.fundoId())
                    .orElseThrow(() -> new IllegalArgumentException("Fundo não encontrado: " + model.fundoId()));

            Empresa empresa = empresaRepository.findById(model.empresaId())
                    .orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada: " + model.empresaId()));

            TipoRecebivel tipo = tipoRecebivelRepository.findById(model.tipoId())
                    .orElseThrow(() -> new IllegalArgumentException("Tipo de recebível não encontrado: " + model.tipoId()));

            recebivel.setValorFace(model.valorFace());
            recebivel.setTaxaBase(model.taxaBase());
            recebivel.setDataVencimento(model.dataVencimento());
            recebivel.setFundo(fundo);
            recebivel.setEmpresa(empresa);
            recebivel.setTipo(tipo);

            recebivel.setValorPresente(fundo == null ? null : calcularValorPresente(
                    model.valorFace(), model.taxaBase(), tipo.getSpread(), fundo.getTaxaBase(),
                    calcularPrazoEmAnos(model.dataVencimento())));
            return toModel(recebivelRepository.save(recebivel));
        });
    }

    @Transactional
    public boolean deletar(Long id) {
        if (!recebivelRepository.existsById(id)) {
            return false;
        }

        recebivelRepository.deleteById(id);
        return true;
    }

    private RecebivelModel toModel(Recebivel recebivel) {
        return new RecebivelModel(
                recebivel.getId(),
                recebivel.getValorFace(),
                recebivel.getValorPresente(),
                recebivel.getDataVencimento(),
                recebivel.getFundo() == null ? null : recebivel.getFundo().getId(),
                recebivel.getTipo().getId(),
                recebivel.getEmpresa().getId(),
                calcularPrazoEmAnos(recebivel.getDataVencimento()),
                recebivel.getTipo().getSpread(),
                recebivel.getTaxaBase()
        );
    }

    public BigDecimal calcularValorPresente(BigDecimal valorFace, BigDecimal taxaBase, BigDecimal spread, BigDecimal prazo) {
        return calcularValorPresente(valorFace, taxaBase, spread, BigDecimal.ZERO, prazo);
    }

    public BigDecimal calcularValorPresente(BigDecimal valorFace,
                                            BigDecimal taxaBaseRecebivel,
                                            BigDecimal spread,
                                            BigDecimal taxaBaseFundo,
                                            BigDecimal prazo) {
        if (valorFace == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal taxaTotal = taxaBaseRecebivel == null ? BigDecimal.ZERO : taxaBaseRecebivel;
        if (spread != null) {
            taxaTotal = taxaTotal.add(spread);
        }
        if (taxaBaseFundo != null) {
            taxaTotal = taxaTotal.add(taxaBaseFundo);
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

    private void validarTaxaBase(RecebivelModel model) {
        if (model.taxaBase() == null) {
            throw new IllegalArgumentException("A taxa base do recebível é obrigatória");
        }
        if (model.taxaBase().compareTo(BigDecimal.ZERO) < 0
                || model.taxaBase().compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("A taxa base do recebível deve estar entre 0% e 100%");
        }
    }
}
