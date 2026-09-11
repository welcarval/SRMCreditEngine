package com.srm.msbackend.config;

import com.srm.msbackend.entities.Conta;
import com.srm.msbackend.entities.Empresa;
import com.srm.msbackend.entities.Fundo;
import com.srm.msbackend.entities.Moeda;
import com.srm.msbackend.entities.Recebivel;
import com.srm.msbackend.entities.Role;
import com.srm.msbackend.entities.Scope;
import com.srm.msbackend.entities.TipoRecebivel;
import com.srm.msbackend.entities.Transacao;
import com.srm.msbackend.repositories.EmpresaRepository;
import com.srm.msbackend.repositories.FundoRepository;
import com.srm.msbackend.repositories.MoedaRepository;
import com.srm.msbackend.repositories.RecebivelRepository;
import com.srm.msbackend.repositories.TipoRecebivelRepository;
import com.srm.msbackend.repositories.RoleRepository;
import com.srm.msbackend.repositories.ScopeRepository;
import com.srm.msbackend.repositories.TransacaoRepository;
import com.srm.msbackend.repositories.UsuarioRepository;
import com.srm.msbackend.services.TransacaoService;
import net.datafaker.Faker;
import com.srm.msbackend.entities.Usuario;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initializeData(
            MoedaRepository moedaRepository,
            FundoRepository fundoRepository,
            EmpresaRepository empresaRepository,
            TipoRecebivelRepository tipoRecebivelRepository,
            RoleRepository roleRepository,
            ScopeRepository scopeRepository,
            UsuarioRepository usuarioRepository,
            RecebivelRepository recebivelRepository,
            TransacaoRepository transacaoRepository,
            TransacaoService transacaoService) {
        return args -> {
            Map<String, String> scopes = Map.ofEntries(
                    Map.entry(AuthorizationScopes.FUNDOS_READ, "Consultar fundos"),
                    Map.entry(AuthorizationScopes.FUNDOS_WRITE, "Criar e alterar fundos"),
                    Map.entry(AuthorizationScopes.RECEBIVEIS_READ, "Consultar recebíveis"),
                    Map.entry(AuthorizationScopes.RECEBIVEIS_WRITE, "Criar e alterar recebíveis"),
                    Map.entry(AuthorizationScopes.EMPRESAS_READ, "Consultar empresas"),
                    Map.entry(AuthorizationScopes.EMPRESAS_WRITE, "Criar e alterar empresas"),
                    Map.entry(AuthorizationScopes.MOEDAS_READ, "Consultar moedas"),
                    Map.entry(AuthorizationScopes.MOEDAS_WRITE, "Criar e alterar moedas"),
                    Map.entry(AuthorizationScopes.TIPOS_RECEBIVEIS_READ, "Consultar tipos de recebível"),
                    Map.entry(AuthorizationScopes.EXTRATOS_READ, "Consultar extratos"),
                    Map.entry(AuthorizationScopes.TRANSACOES_READ, "Consultar transações"),
                    Map.entry(AuthorizationScopes.TRANSACOES_WRITE, "Criar e alterar transações"),
                    Map.entry(AuthorizationScopes.USUARIOS_READ, "Consultar usuários"),
                    Map.entry(AuthorizationScopes.USUARIOS_WRITE, "Criar e alterar usuários"),
                    Map.entry(AuthorizationScopes.PERFIL_READ, "Consultar o próprio perfil"));
            List<Scope> todosScopes = scopes.entrySet().stream()
                    .map(entry -> scopeRepository.findByCodigoIgnoreCase(entry.getKey())
                            .orElseGet(() -> scopeRepository.save(new Scope(entry.getKey(), entry.getValue()))))
                    .toList();

            Role admin = roleRepository.findByCodigoIgnoreCase("ADMIN")
                    .orElseGet(() -> roleRepository.save(new Role("ADMIN", "Administrador")));
            admin.getScopes().clear();
            admin.getScopes().addAll(todosScopes);
            roleRepository.save(admin);

            Role operador = roleRepository.findByCodigoIgnoreCase("OPERADOR")
                    .orElseGet(() -> roleRepository.save(new Role("OPERADOR", "Operador")));
            operador.getScopes().clear();
            operador.getScopes().addAll(scopesParaOperador(todosScopes));
            roleRepository.save(operador);

            usuarioRepository.findByEmailIgnoreCase("srm.admin@localhost")
                    .orElseGet(() -> usuarioRepository.save(
                            new Usuario("SRM Admin", "srm.admin@localhost", admin)));
            usuarioRepository.findByEmailIgnoreCase("srm.user@localhost")
                    .orElseGet(() -> usuarioRepository.save(
                            new Usuario("SRM User", "srm.user@localhost", operador)));

            if (recebivelRepository.count() > 0) {
                return;
            }

            Faker faker = new Faker(new Locale("pt-BR"));
            List<Moeda> moedas = moedaRepository.saveAll(List.of(
                    new Moeda("BRL", "Real brasileiro", new BigDecimal("0.20000000")),
                    new Moeda("USD", "Dólar americano", new BigDecimal("1.00000000")),
                    new Moeda("EUR", "Euro", new BigDecimal("1.08000000")),
                    new Moeda("GBP", "Libra esterlina", new BigDecimal("1.27000000")),
                    new Moeda("JPY", "Iene japonês", new BigDecimal("0.00680000"))
            ));
            Moeda real = moedas.get(0);

            List<Fundo> fundos = fundoRepository.saveAll(List.of(
                    criarFundo(faker.company().name() + " Fundo", cnpj(11), "0.085000", real, "ALPHA"),
                    criarFundo(faker.company().name() + " Fundo", cnpj(22), "0.095000", real, "BETA"),
                    criarFundo(faker.company().name() + " Fundo", cnpj(33), "0.105000", real, "GAMMA")
            ));

            List<Empresa> empresas = empresaRepository.saveAll(List.of(
                    criarEmpresa(faker.company().name(), cnpj(44), moedas.get(0), "EMP01"),
                    criarEmpresa(faker.company().name(), cnpj(55), moedas.get(1), "EMP02"),
                    criarEmpresa(faker.company().name(), cnpj(66), moedas.get(2), "EMP03"),
                    criarEmpresa(faker.company().name(), cnpj(77), moedas.get(3), "EMP04"),
                    criarEmpresa(faker.company().name(), cnpj(88), moedas.get(4), "EMP05")
            ));

            List<TipoRecebivel> tipos = tipoRecebivelRepository.saveAll(List.of(
                    new TipoRecebivel("Duplicata mercantil", new BigDecimal("0.012000")),
                    new TipoRecebivel("Nota promissoria", new BigDecimal("0.018000")),
                    new TipoRecebivel("Contrato de servicos", new BigDecimal("0.025000"))
            ));

            List<Recebivel> recebiveis = IntStream.rangeClosed(1, 50)
                    .mapToObj(index -> criarRecebivel(index, empresas, tipos))
                    .toList();

            recebivelRepository.saveAll(recebiveis);

            List<Transacao> transacoes = IntStream.rangeClosed(1, 30)
                    .mapToObj(index -> criarTransacao(index, fundos, empresas, transacaoService))
                    .toList();
            transacaoRepository.saveAll(transacoes);
        };
    }

    private List<Scope> scopesParaOperador(List<Scope> todosScopes) {
        List<String> codigos = List.of(
                AuthorizationScopes.FUNDOS_READ,
                AuthorizationScopes.RECEBIVEIS_READ,
                AuthorizationScopes.RECEBIVEIS_WRITE,
                AuthorizationScopes.EMPRESAS_READ,
                AuthorizationScopes.MOEDAS_READ,
                AuthorizationScopes.TIPOS_RECEBIVEIS_READ,
                AuthorizationScopes.EXTRATOS_READ,
                AuthorizationScopes.TRANSACOES_READ,
                AuthorizationScopes.TRANSACOES_WRITE,
                AuthorizationScopes.PERFIL_READ);
        return todosScopes.stream().filter(scope -> codigos.contains(scope.getCodigo())).toList();
    }

    private String cnpj(int seed) {
        return "%02d.%03d.%03d/0001-%02d".formatted(seed, seed, seed, seed);
    }

    private Fundo criarFundo(String nome, String cnpj, String taxaBase, Moeda moeda, String identificador) {
        Conta conta = new Conta(identificador, "Banco SRM", moeda, new BigDecimal("100000.00"));
        return new Fundo(nome, cnpj, new BigDecimal(taxaBase), conta);
    }

    private Empresa criarEmpresa(String razaoSocial, String cnpj, Moeda moeda, String identificador) {
        Conta conta = new Conta(identificador, "Banco SRM", moeda);
        return new Empresa(razaoSocial, cnpj, conta);
    }

    private Recebivel criarRecebivel(
            int index,
            List<Empresa> empresas,
            List<TipoRecebivel> tipos) {
        Empresa empresa = empresas.get((index - 1) % empresas.size());
        TipoRecebivel tipo = tipos.get((index - 1) % tipos.size());
        BigDecimal valorFace = BigDecimal.valueOf(10_000L + (index * 1_250L))
                .setScale(2);
        BigDecimal taxaBase = new BigDecimal("0.070000");
        LocalDate vencimento = LocalDate.now().plusDays(30L + ((long) index * 15L));

        return new Recebivel(null, valorFace, vencimento, null, tipo, empresa, taxaBase);
    }

    private Transacao criarTransacao(
            int index,
            List<Fundo> fundos,
            List<Empresa> empresas,
            TransacaoService transacaoService) {
        Fundo fundo = fundos.get((index - 1) % fundos.size());
        Empresa empresa = empresas.get((index - 1) % empresas.size());
        BigDecimal valor = BigDecimal.valueOf(500L + (index * 25L)).setScale(2);
        Transacao transacao = new Transacao(
                valor,
                LocalDateTime.now().minusDays(index),
                fundo.getConta(),
                empresa.getConta());
        transacaoService.executar(transacao);
        return transacao;
    }
}
