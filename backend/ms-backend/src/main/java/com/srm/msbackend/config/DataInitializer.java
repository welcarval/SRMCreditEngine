package com.srm.msbackend.config;

import com.srm.msbackend.entities.Conta;
import com.srm.msbackend.entities.Empresa;
import com.srm.msbackend.entities.Fundo;
import com.srm.msbackend.entities.Moeda;
import com.srm.msbackend.entities.Recebivel;
import com.srm.msbackend.entities.TipoRecebivel;
import com.srm.msbackend.repositories.EmpresaRepository;
import com.srm.msbackend.repositories.FundoRepository;
import com.srm.msbackend.repositories.MoedaRepository;
import com.srm.msbackend.repositories.RecebivelRepository;
import com.srm.msbackend.repositories.TipoRecebivelRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initializeData(
            MoedaRepository moedaRepository,
            FundoRepository fundoRepository,
            EmpresaRepository empresaRepository,
            TipoRecebivelRepository tipoRecebivelRepository,
            RecebivelRepository recebivelRepository) {
        return args -> {
            if (recebivelRepository.count() > 0) {
                return;
            }

            Moeda real = moedaRepository.save(
                    new Moeda("BRL", "Real brasileiro", new BigDecimal("1.00000000")));

            List<Fundo> fundos = fundoRepository.saveAll(List.of(
                    criarFundo("Fundo SRM Alpha", "11.111.111/0001-11", "0.085000", real, "ALPHA"),
                    criarFundo("Fundo SRM Beta", "22.222.222/0001-22", "0.095000", real, "BETA"),
                    criarFundo("Fundo SRM Gamma", "33.333.333/0001-33", "0.105000", real, "GAMMA")
            ));

            List<Empresa> empresas = empresaRepository.saveAll(List.of(
                    criarEmpresa("Empresa Mock 01", "44.444.444/0001-44", real, "EMP01"),
                    criarEmpresa("Empresa Mock 02", "55.555.555/0001-55", real, "EMP02"),
                    criarEmpresa("Empresa Mock 03", "66.666.666/0001-66", real, "EMP03"),
                    criarEmpresa("Empresa Mock 04", "77.777.777/0001-77", real, "EMP04"),
                    criarEmpresa("Empresa Mock 05", "88.888.888/0001-88", real, "EMP05")
            ));

            List<TipoRecebivel> tipos = tipoRecebivelRepository.saveAll(List.of(
                    new TipoRecebivel("Duplicata mercantil", new BigDecimal("0.012000")),
                    new TipoRecebivel("Nota promissoria", new BigDecimal("0.018000")),
                    new TipoRecebivel("Contrato de servicos", new BigDecimal("0.025000"))
            ));

            List<Recebivel> recebiveis = IntStream.rangeClosed(1, 50)
                    .mapToObj(index -> criarRecebivel(index, fundos, empresas, tipos))
                    .toList();

            recebivelRepository.saveAll(recebiveis);
        };
    }

    private Fundo criarFundo(String nome, String cnpj, String taxaBase, Moeda moeda, String identificador) {
        Conta conta = new Conta(identificador, "Banco SRM", moeda);
        return new Fundo(nome, cnpj, new BigDecimal(taxaBase), conta);
    }

    private Empresa criarEmpresa(String razaoSocial, String cnpj, Moeda moeda, String identificador) {
        Conta conta = new Conta(identificador, "Banco SRM", moeda);
        return new Empresa(razaoSocial, cnpj, conta);
    }

    private Recebivel criarRecebivel(
            int index,
            List<Fundo> fundos,
            List<Empresa> empresas,
            List<TipoRecebivel> tipos) {
        Fundo fundo = fundos.get((index - 1) % fundos.size());
        Empresa empresa = empresas.get((index - 1) % empresas.size());
        TipoRecebivel tipo = tipos.get((index - 1) % tipos.size());
        BigDecimal valorFace = BigDecimal.valueOf(10_000L + (index * 1_250L))
                .setScale(2);
        LocalDate vencimento = LocalDate.now().plusDays(30L + ((long) index * 15L));
        BigDecimal prazoEmAnos = BigDecimal.valueOf(30L + ((long) index * 15L))
                .divide(BigDecimal.valueOf(365), 10, RoundingMode.HALF_UP);
        BigDecimal taxaTotal = fundo.getTaxaBase().add(tipo.getSpread());
        BigDecimal valorPresente = valorFace.divide(
                BigDecimal.valueOf(Math.pow(BigDecimal.ONE.add(taxaTotal).doubleValue(),
                        prazoEmAnos.doubleValue())),
                2,
                RoundingMode.HALF_UP);

        return new Recebivel(valorPresente, valorFace, vencimento, null, tipo, empresa);
    }
}
