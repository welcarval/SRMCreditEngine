# SRM Credit Engine

O projeto foi criado inteiramente utilizando IAs. As IAs utilizadas foram github copilot e OpenAi codex. Em poucos casos eu realmente digitei código, inclusive a maior parte desse README.md foi gerada por IA. Meu trabalho no projeto foi, primeiramente, estabelecer o escopo do projeto, eu tive algumas dúvidas quanto ao que era necessário fazer, no entanto, como eu tinha o desejo de fazer um sistema financeiro próprio, principalmente a parte de segurança, eu talvez tenha feito coisas que não foram pedidas, assim como deixei de fazer algumas coisas que foram pedidas. 

Enfim, primeiramente eu estabeleci que para se poder cadastrar, precificar, e comprar recebíveis, era necessário ter um fundo ao qual se compraria o recebível, por isso a primeira relação de entidade que fiz foi essa, de um fundo poder ter 0 ou muitos recebíveis, cada recebível tem um tipo, e esse tipo influencia inclusive o risco, e consequentemente a taxa base de calculo do seu valor presente, o spread, por isso criei também uma entidade parametrização chamada "Tipo Recebível". Como o projeto envolve compra de recebíveis, eu criei também a entidade Empresa, ao qual seria a emissora de recebíveis, podendo emitir 0 ou muitos recebíveis.

Para se haver transações financeiras, eu criei as entidades Conta e Transação, de modo que, tanto os fundos, quanto as empresas devessem ter uma conta, pois as transações seriam feitas entre elas, ou seja, um fundo, ao comprar um recebível, faria uma transação de sua conta para a conta da empresa emissora do recebivel. E como o projeto buscava ser multimoedas, eu criei a entidade Moeda também, de modo que uma conta obrigatóriamente devesse ter uma moeda, cujo valor cambial fosse calculado sobre o dolar, sendo assim, em transações, caso as contas fossem de diferentes moedas, o sistema deveria ser capaz de fazer a conversão cambial para que os valores fosse transferidos corretamente.

Até então seguir com esse escopo seria de certa forma suficiente para a conclusão do projeto, porém, como eu disse inicialmente, eu tinha o desejo de fazer um sistema assim, principalmente relacionado a segurança, por isso criei mas entidades que buscassem essencialmente o controle de acesso as funcionalidades do sistema, essas entidades foram Usuario, Role e Scope, que seguem o padrão RBAC (Role Based Access Control), sendo assim eu poderia fazer um sistema capaz de gerir muitos operadores e administradores, onde os administradores dariam as permissões necessárias aos operadores para fazerem alterações no sistema.

Como é padrão na industria, eu não fiz o sistema de autenticação na própria aplicação, eu preferi utilziar um servidor externo, no caso eu usei o sistema opensource keycloak, que eu ja tinha um conhecimento básico, e que eu tambem poderiam embutir no projeto como um container docker onde seria possível testar de forma local.

O sistema funciona solicitando login ao abrir o portal, sem estar logado o usurio não consegue acessar nenhuma funcionalidade do sistema. Além disso, para cada funcionalidade do portal, o usuário deve ter uma role que contenha os scopes necessário para acessá-la. 

Infelizmente por conta do tempo eu não fui capaz de refinar essa parte como eu gostaria, portanto são criados por padrão apenas dois usuarios, um Admin, e um Operador, o operador por padrão não consegue fazer nada, sendo necessrio que o Admin faça o login no sistema para que este consiga fazer as ações necessárias.

para logar no sistema como admin:
- **username: srm.admin**
- **password: admin123**

para logar como operador:
- **username: srm.user**
- **password: user123**

A parte de documentação da API e os testes também foram gerados por IA, por conta do prazo eu também não consegui validar a corretude dos dois.

Abaixo segue o README.md gerado pela própria IA

Portal operacional para administração de fundos, recebíveis, empresas, transações e extratos. O projeto é composto por uma API Java protegida por OAuth 2.0/OpenID Connect, uma aplicação web Svelte e serviços de infraestrutura executados com Docker Compose.

## Arquitetura

```text
Navegador
   │
   ├── Frontend SvelteKit + nginx (http://localhost)
   │       └── /api/* ───────────────┐
   │                                 ▼
   ├── Keycloak (http://localhost:8180)   Backend Spring Boot (http://localhost:8080)
   │         │                                      │
   │         └── tokens OIDC/JWT                    ▼
   │                                            PostgreSQL
   └──────────────────────────────────────────────────
```

O nginx entrega os arquivos estáticos do frontend e faz proxy de `/api/*` para o backend. O navegador se autentica diretamente no Keycloak; o backend valida o JWT recebido em cada chamada protegida.

## Tecnologias

| Camada | Tecnologias |
| --- | --- |
| Frontend | Svelte 5, SvelteKit, TypeScript, Vite, `oidc-client-ts`, nginx |
| Backend | Java 21, Spring Boot 4, Spring MVC, Spring Security OAuth2 Resource Server, Spring Data JPA, Hibernate |
| Dados | PostgreSQL 16 |
| Identidade | Keycloak com OpenID Connect / OAuth 2.0 |
| Documentação de API | Springdoc OpenAPI / Swagger UI |
| Qualidade | Vitest, Playwright, JUnit 5, Mockito e JaCoCo |
| Execução | Docker e Docker Compose |

## Backend

O backend fica em [`backend/ms-backend`](backend/ms-backend) e está organizado em camadas:

- `controllers`: rotas REST e regras de autorização por endpoint;
- `services`: regras de negócio, cálculo e autorização;
- `repositories`: acesso aos dados com Spring Data JPA;
- `entities`: mapeamento JPA das tabelas;
- `models`: DTOs usados pela API;
- `config`: segurança, OpenAPI, escopos de autorização e carga inicial de dados.

### Domínio e relacionamentos

```text
Moeda 1 ─── N Conta
Conta 1 ─── 1 Fundo
Conta 1 ─── 1 Empresa

Empresa 1 ─── N Recebível N ─── 1 TipoRecebível
Fundo   1 ─── N Recebível (opcional enquanto o recebível não foi comprado)

Conta 1 ─── N Transação (origem)
Conta 1 ─── N Transação (destino)

Usuário N ─── N Fundo       (acesso a fundos)
Usuário N ─── N Role
Role    N ─── N Scope
Usuário N ─── N Scope       (permissões adicionais individuais)
```

Principais entidades:

- **Moeda**: código, nome e taxa de câmbio para dólar.
- **Conta**: identificador, instituição, saldo e moeda. É a origem/destino de transações e pertence exclusivamente a um fundo ou empresa.
- **Fundo**: veículo de crédito com CNPJ, taxa base, conta própria, recebíveis e usuários autorizados.
- **Empresa**: cedente dos recebíveis, com CNPJ e conta própria.
- **TipoRecebível**: categoria e spread de um recebível.
- **Recebível**: valor de face, valor presente, vencimento, taxa base, empresa, tipo e, opcionalmente, fundo comprador.
- **Transação**: movimentação entre duas contas, com valor, data/hora e status (`PENDENTE`, `SUCESSO` ou `FALHA`).
- **Usuário, Role e Scope**: estrutura de autorização da aplicação.

### Dados iniciais

No startup, `DataInitializer` cria as roles `ADMIN` e `OPERADOR`, os scopes conhecidos, os usuários locais e dados de demonstração (moedas, fundos, empresas, recebíveis e transações). A configuração atual usa `spring.jpa.hibernate.ddl-auto=create-drop`, portanto o esquema e os dados são recriados quando o backend é reiniciado.

### API e autorização

As rotas são protegidas com `@PreAuthorize`. `FundoAuthorizationService` identifica o usuário pelo claim `email` (ou `preferred_username`) do JWT, encontra-o no banco e autoriza a operação por scope. Nas operações que tratam um fundo específico, também verifica se o usuário possui acesso àquele fundo.

Os scopes seguem o padrão `recurso:ação`, por exemplo:

- `fundos:read`, `fundos:write`
- `recebiveis:read`, `recebiveis:write`
- `empresas:read`, `empresas:write`
- `moedas:read`, `moedas:write`
- `tipos-recebiveis:read`
- `extratos:read`
- `transacoes:read`, `transacoes:write`
- `usuarios:read`, `usuarios:write`
- `perfil:read`

O admin recebe todos os scopes. O operador recebe o conjunto operacional padrão e pode receber scopes individuais adicionais na tela administrativa. Scopes adicionados diretamente ao usuário são somados aos scopes herdados de suas roles.

Principais recursos da API:

- `/api/fundos`, `/api/recebiveis`, `/api/empresas`, `/api/moedas` e `/api/tipos-recebiveis`: cadastros do domínio;
- `/api/transacoes`: operações financeiras entre contas;
- `/api/extratos`: consulta e exportação de extratos;
- `/api/usuarios/me`: perfil e permissões do usuário autenticado;
- `/api/usuarios`: usuários e associação a fundos;
- `/api/scopes` e `/api/usuarios/{id}/scopes`: listagem e atribuição de scopes; ambos restritos a administradores.

Com o sistema em execução, a documentação interativa da API está disponível em `http://localhost:8080/swagger-ui.html`.

## Frontend

O frontend está em [`frontend`](frontend), é uma SPA estática gerada por SvelteKit e servida por nginx.

Funcionalidades principais:

- Login e logout pelo Keycloak;
- Dashboard operacional;
- Gestão de fundos, recebíveis, empresas e tipos de recebível;
- Compra/associação de recebíveis a fundos;
- Extratos e exportação em formatos suportados pela API;
- Administração de acessos a fundos por usuário em `/usuarios`;
- Administração de permissões adicionais por usuário em `/permissoes`;
- Navegação condicional por perfil: itens administrativos, extratos e permissões são exibidos somente para administradores.

O estado da sessão OIDC é mantido no `localStorage`. O cliente envia o access token como `Authorization: Bearer <token>` em cada chamada à API. Respostas `401` encerram a sessão; outros erros da API não devem provocar logout automático.

## Autenticação com Keycloak

O realm [`srm-credit-engine`](keycloak/srm-credit-engine-realm.json) é importado automaticamente pelo Compose. O cliente público OIDC configurado é `srm-frontend`, usando Authorization Code Flow com PKCE administrado por `oidc-client-ts`.

URLs relevantes:

- Frontend: `http://localhost`
- Keycloak: `http://localhost:8180`
- Console administrativo do Keycloak: `http://localhost:8180/admin`
- Realm: `srm-credit-engine`

Credenciais de demonstração:

| Perfil | Usuário | Senha |
| --- | --- | --- |
| Aplicação — administrador | `srm.admin` | `admin123` |
| Aplicação — operador | `srm.user` | `user123` |
| Console administrativo do Keycloak | `admin` | `admin` |

Essas credenciais são apenas para desenvolvimento local. Nunca as reutilize em ambiente produtivo.

## Banco de dados

O PostgreSQL é iniciado com:

- host exposto: `localhost:5432`;
- banco: `srm`;
- usuário: `srm`;
- senha: `srm`.

Os dados do container são montados em [`postgres/data`](postgres/data). O backend, por sua vez, usa a rede interna do Compose com a URL `jdbc:postgresql://postgres:5432/srm`.

## Como executar

### Pré-requisitos

Para a forma recomendada de execução, instale:

1. [Docker Engine ou Docker Desktop](https://docs.docker.com/get-docker/);
2. Docker Compose v2 (normalmente já incluído no Docker Desktop).

Confirme a instalação:

```bash
docker --version
docker compose version
```

### Subir todos os serviços

Na raiz do repositório, execute:

```bash
docker compose up -d --build
```

O comando constrói as imagens do backend e frontend e inicia Keycloak, PostgreSQL, backend e nginx. Aguarde o backend concluir o startup antes de testar a aplicação.

Confira os containers:

```bash
docker compose ps
```

Acompanhe os logs, se necessário:

```bash
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f keycloak
```

Abra `http://localhost` e entre com uma das contas de demonstração.

### Parar ou reconstruir

```bash
# Para os containers e preserva o volume/diretório de dados
docker compose down

# Recria imagens e containers após alterações de código
docker compose up -d --build
```

### Reiniciar do zero

Use este comando apenas se puder descartar os dados locais do Postgres:

```bash
docker compose down -v
docker compose up -d --build
```

Se o diretório `postgres/data` persistir dados de uma instalação anterior incompatível, remova-o manualmente somente após confirmar que os dados podem ser descartados.

## Desenvolvimento e testes

O Docker é suficiente para rodar o sistema completo. Para trabalhar em cada aplicação separadamente, instale também Node.js 22+ para o frontend e Java 21 para o backend.

```bash
# Frontend
cd frontend
npm ci
npm run dev
npm run check
npm run test:unit

# Backend
cd backend/ms-backend
./gradlew bootRun
./gradlew test
```

Ao executar o backend fora do Docker, ajuste a URL do banco e do issuer do Keycloak conforme seu ambiente. No Compose, essas configurações já são fornecidas por variáveis de ambiente.

## Estrutura do repositório

```text
.
├── backend/ms-backend/    # API Spring Boot
├── frontend/              # Portal SvelteKit e configuração nginx
├── keycloak/              # Realm e usuários de desenvolvimento
├── postgres/data/         # Dados locais do Postgres (volume montado)
└── docker-compose.yml     # Orquestração completa do ambiente
```
