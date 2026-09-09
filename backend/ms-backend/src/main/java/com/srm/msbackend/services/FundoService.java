package com.srm.msbackend.services;

import com.srm.msbackend.entities.Conta;
import com.srm.msbackend.entities.Fundo;
import com.srm.msbackend.entities.Recebivel;
import com.srm.msbackend.entities.Transacao;
import com.srm.msbackend.models.FundoModel;
import com.srm.msbackend.models.RecebivelModel;
import com.srm.msbackend.repositories.ContaRepository;
import com.srm.msbackend.repositories.FundoRepository;
import com.srm.msbackend.repositories.RecebivelRepository;
import com.srm.msbackend.repositories.TransacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class FundoService {
    private final FundoRepository fundoRepository;
    private final ContaRepository contaRepository;
    private final RecebivelRepository recebivelRepository;
    private final TransacaoRepository transacaoRepository;
    private final TransacaoService transacaoService;

    public FundoService(FundoRepository fundoRepository,
                        ContaRepository contaRepository,
                        RecebivelRepository recebivelRepository,
                        TransacaoRepository transacaoRepository,
                        TransacaoService transacaoService) {
        this.fundoRepository = fundoRepository;
        this.contaRepository = contaRepository;
        this.recebivelRepository = recebivelRepository;
        this.transacaoRepository = transacaoRepository;
        this.transacaoService = transacaoService;
    }

    @Transactional(readOnly = true)
    public List<FundoModel> listar() {
        return fundoRepository.findAll().stream().map(this::toModel).toList();
    }

    @Transactional(readOnly = true)
    public Optional<FundoModel> buscarPorId(Long id) {
        return fundoRepository.findById(id).map(this::toModel);
    }

    @Transactional
    public FundoModel salvar(FundoModel model) {
        Fundo fundo = new Fundo(model.nome(), model.cnpj(), model.taxaBase(), null);

        if (model.contaId() != null) {
            Conta conta = contaRepository.findById(model.contaId())
                    .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada: " + model.contaId()));
            fundo.setConta(conta);
        }

        return toModel(fundoRepository.save(fundo));
    }

    @Transactional
    public Optional<FundoModel> atualizar(Long id, FundoModel model) {
        return fundoRepository.findById(id).map(fundo -> {
            fundo.setNome(model.nome());
            fundo.setCnpj(model.cnpj());
            fundo.setTaxaBase(model.taxaBase());

            if (model.contaId() != null) {
                Conta conta = contaRepository.findById(model.contaId())
                        .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada: " + model.contaId()));
                fundo.setConta(conta);
            }

            return toModel(fundoRepository.save(fundo));
        });
    }

    @Transactional
    public RecebivelModel adicionarRecebivel(Long fundoId, Long recebivelId) {
        Fundo fundo = fundoRepository.findById(fundoId)
                .orElseThrow(() -> new IllegalArgumentException("Fundo não encontrado: " + fundoId));
        Recebivel recebivel = recebivelRepository.findById(recebivelId)
                .orElseThrow(() -> new IllegalArgumentException("Recebível não encontrado: " + recebivelId));

        if (recebivel.getFundo() != null) {
            throw new IllegalStateException("O recebível já está associado a um fundo");
        }
        if (fundo.getConta() == null) {
            throw new IllegalStateException("O fundo não possui conta para realizar a compra");
        }
        if (recebivel.getEmpresa().getConta() == null) {
            throw new IllegalStateException("A empresa não possui conta para receber a compra");
        }

        recebivel.setFundo(fundo);

        Transacao transacao = new Transacao(
                recebivel.getValorPresente(),
                LocalDateTime.now(),
                fundo.getConta(),
                recebivel.getEmpresa().getConta()
        );
        transacaoService.executar(transacao);
        transacaoRepository.save(transacao);

        return toRecebivelModel(recebivelRepository.save(recebivel));
    }

    @Transactional
    public boolean deletar(Long id) {
        if (!fundoRepository.existsById(id)) {
            return false;
        }

        fundoRepository.deleteById(id);
        return true;
    }

    private FundoModel toModel(Fundo fundo) {
        return new FundoModel(
                fundo.getId(),
                fundo.getNome(),
                fundo.getCnpj(),
                fundo.getTaxaBase(),
                fundo.getConta() == null ? null : fundo.getConta().getId()
        );
    }

    private RecebivelModel toRecebivelModel(Recebivel recebivel) {
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
                recebivel.getFundo() == null ? null : recebivel.getFundo().getTaxaBase()
        );
    }

    private BigDecimal calcularPrazoEmAnos(LocalDate dataVencimento) {
        if (dataVencimento == null) {
            return BigDecimal.ONE;
        }

        long dias = ChronoUnit.DAYS.between(LocalDate.now(), dataVencimento);
        return BigDecimal.valueOf(dias)
                .divide(BigDecimal.valueOf(365), 10, RoundingMode.HALF_UP)
                .max(BigDecimal.ZERO);
    }
}
