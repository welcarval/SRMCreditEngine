<script>
    import {onMount} from 'svelte';
    import {api} from './lib/api.js';

    const nav = [
        {path: '/', label: 'Visão geral', icon: 'grid'},
        {path: '/recebiveis', label: 'Recebíveis', icon: 'file'},
        {path: '/fundos', label: 'Fundos', icon: 'layers'}
    ];

    let route = window.location.hash.slice(1) || '/';
    let funds = [];
    let receivables = [];
    let companies = [];
    let loading = true;
    let error = '';
    let notice = '';
    let query = '';
    let showFundForm = false;
    let showReceivableForm = false;
    let editingFund = null;
    let editingReceivable = null;
    let activeTab = 'todos';

    let fundForm = {nome: '', cnpj: '', taxaBase: '', contaId: ''};
    let receivableForm = {
        valorFace: '', dataVencimento: '', fundoId: '', tipoId: '', empresaId: '',
        prazo: '', spread: '', taxaBase: ''
    };

    const currency = (value) => new Intl.NumberFormat('pt-BR', {
        style: 'currency',
        currency: 'BRL'
    }).format(Number(value || 0));
    const percent = (value) => `${(Number(value || 0) * 100).toFixed(2).replace('.', ',')}%`;
    const date = (value) => value ? new Intl.DateTimeFormat('pt-BR').format(new Date(`${value}T00:00:00`)) : '—';
    const fundName = (item) => item?.fundo?.nome || funds.find((fund) => String(fund.id) === String(item?.fundoId))?.nome || '—';
    const companyName = (item) => item?.empresa?.razaoSocial || companies.find((company) => String(company.id) === String(item?.empresaId))?.razaoSocial || '—';

    $: path = route.split('/').filter(Boolean);
    $: currentPage = path[0] || 'dashboard';
    $: filteredFunds = funds.filter((fund) => `${fund.nome} ${fund.cnpj}`.toLowerCase().includes(query.toLowerCase()));
    $: filteredReceivables = receivables.filter((item) => {
        const statusMatch = activeTab === 'todos' || (activeTab === 'vencendo' && daysToMaturity(item.dataVencimento) <= 30) || (activeTab === 'atrasados' && daysToMaturity(item.dataVencimento) < 0);
        return statusMatch && `${item.id} ${fundName(item)} ${companyName(item)}`.toLowerCase().includes(query.toLowerCase());
    });
    $: faceTotal = receivables.reduce((total, item) => total + Number(item.valorFace || 0), 0);
    $: presentTotal = receivables.reduce((total, item) => total + Number(item.valorPresente || 0), 0);
    $: maturityPreview = receivableForm.dataVencimento ? daysToMaturity(receivableForm.dataVencimento) / 365 : 1;
    $: calculatedPresentValue = receivableForm.valorFace ? Number(receivableForm.valorFace) / Math.pow(1 + Number(receivableForm.taxaBase || 0) + Number(receivableForm.spread || 0), Math.max(maturityPreview, 0)) : 0;

    function daysToMaturity(value) {
        if (!value) return 0;
        return Math.ceil((new Date(`${value}T00:00:00`) - new Date()) / 86400000);
    }

    function navigate(path) {
        window.location.hash = path;
        route = path;
        query = '';
        closeForms();
    }

    async function loadData() {
        loading = true;
        error = '';
        try {
            const results = await Promise.allSettled([api.funds(), api.receivables(), api.companies()]);
            if (results[0].status === 'fulfilled') funds = results[0].value || [];
            if (results[1].status === 'fulfilled') receivables = results[1].value || [];
            if (results[2].status === 'fulfilled') companies = results[2].value || [];
            const failed = results.find((result) => result.status === 'rejected');
            if (failed && !funds.length && !receivables.length) error = 'Não foi possível conectar ao ms-backend. Confira a API e o token do operador.';
        } catch (err) {
            error = err.message;
        } finally {
            loading = false;
        }
    }

    function editFund(fund) {
        editingFund = fund;
        fundForm = {
            nome: fund.nome || '',
            cnpj: fund.cnpj || '',
            taxaBase: fund.taxaBase ?? '',
            contaId: fund.conta?.id ?? fund.contaId ?? ''
        };
        showFundForm = true;
    }

    function editReceivable(item) {
        editingReceivable = item;
        receivableForm = {
            valorFace: item.valorFace ?? '', dataVencimento: item.dataVencimento || '',
            fundoId: item.fundo?.id ?? item.fundoId ?? '', tipoId: item.tipo?.id ?? item.tipoId ?? '',
            empresaId: item.empresa?.id ?? item.empresaId ?? '', prazo: item.prazo ?? '',
            spread: item.spread ?? item.tipo?.spread ?? '', taxaBase: item.taxaBase ?? item.fundo?.taxaBase ?? ''
        };
        showReceivableForm = true;
    }

    function closeForms() {
        showFundForm = false;
        showReceivableForm = false;
        editingFund = null;
        editingReceivable = null;
    }

    function newFund() {
        fundForm = {nome: '', cnpj: '', taxaBase: '', contaId: ''};
        editingFund = null;
        showFundForm = true;
    }

    function newReceivable() {
        receivableForm = {
            valorFace: '',
            dataVencimento: '',
            fundoId: '',
            tipoId: '',
            empresaId: '',
            prazo: '',
            spread: '',
            taxaBase: ''
        };
        editingReceivable = null;
        showReceivableForm = true;
    }

    async function saveFund() {
        notice = '';
        try {
            const payload = {
                id: editingFund?.id,
                nome: fundForm.nome,
                cnpj: fundForm.cnpj,
                taxaBase: Number(fundForm.taxaBase),
                contaId: fundForm.contaId ? Number(fundForm.contaId) : null
            };
            const saved = editingFund ? await api.updateFund(editingFund.id, payload) : await api.createFund(payload);
            funds = editingFund ? funds.map((fund) => fund.id === saved.id ? saved : fund) : [...funds, saved];
            closeForms();
            notice = editingFund ? 'Fundo atualizado com sucesso.' : 'Fundo cadastrado com sucesso.';
        } catch (err) {
            error = err.message;
        }
    }

    async function saveReceivable() {
        notice = '';
        try {
            const payload = {
                id: editingReceivable?.id, valorFace: Number(receivableForm.valorFace),
                valorPresente: calculatedPresentValue, dataVencimento: receivableForm.dataVencimento,
                fundoId: Number(receivableForm.fundoId), tipoId: Number(receivableForm.tipoId),
                empresaId: Number(receivableForm.empresaId), prazo: Number(receivableForm.prazo || maturityPreview),
                spread: Number(receivableForm.spread || 0), taxaBase: Number(receivableForm.taxaBase || 0)
            };
            const saved = editingReceivable ? await api.updateReceivable(editingReceivable.id, payload) : await api.createReceivable(payload);
            receivables = editingReceivable ? receivables.map((item) => item.id === saved.id ? saved : item) : [...receivables, saved];
            closeForms();
            notice = editingReceivable ? 'Recebível atualizado com sucesso.' : 'Recebível cadastrado com sucesso.';
        } catch (err) {
            error = err.message;
        }
    }

    async function removeFund(id) {
        if (!confirm('Excluir este fundo? Esta ação não pode ser desfeita.')) return;
        try {
            await api.deleteFund(id);
            funds = funds.filter((fund) => fund.id !== id);
            notice = 'Fundo removido.';
        } catch (err) {
            error = err.message;
        }
    }

    async function removeReceivable(id) {
        if (!confirm('Excluir este recebível? Esta ação não pode ser desfeita.')) return;
        try {
            await api.deleteReceivable(id);
            receivables = receivables.filter((item) => item.id !== id);
            notice = 'Recebível removido.';
        } catch (err) {
            error = err.message;
        }
    }

    onMount(() => {
        const onHashChange = () => {
            route = window.location.hash.slice(1) || '/';
        };
        window.addEventListener('hashchange', onHashChange);
        loadData();
        return () => window.removeEventListener('hashchange', onHashChange);
    });
</script>

<svelte:head>
    <title>{currentPage === 'dashboard' ? 'Visão geral' : currentPage === 'fundos' ? 'Fundos' : 'Recebíveis'} | Operator Portal</title>
</svelte:head>

<div class="app-shell">
    <aside class="sidebar">
        <div class="brand"><span
                class="brand-mark">S</span><span><strong>SRM</strong><small>OPERATOR PORTAL</small></span></div>
        <div class="workspace"><span class="status-dot"></span><span>Ambiente operacional</span><span
                class="chevron">⌄</span></div>
        <nav>
            <p class="nav-label">NAVEGAÇÃO</p>
            {#each nav as item}
                <a class:active={(item.path === '/' && currentPage === 'dashboard') || item.path.slice(1) === currentPage}
                   href={`#${item.path}`}><span
                        class="nav-icon">{item.icon === 'grid' ? '▦' : item.icon === 'file' ? '▤' : '▱'}</span>{item.label}
                </a>
            {/each}
        </nav>
        <div class="sidebar-bottom">
            <div class="help">?<span>Central de ajuda</span></div>
            <div class="user"><span
                    class="avatar">OM</span><span><strong>Operador Master</strong><small>Administrador</small></span><span
                    class="chevron">⌄</span></div>
        </div>
    </aside>

    <main class="main">
        <header class="topbar">
            <div class="breadcrumbs">
                <span>Operações</span><b>/</b><strong>{currentPage === 'dashboard' ? 'Visão geral' : currentPage === 'fundos' ? 'Fundos' : 'Recebíveis'}</strong>
            </div>
            <div class="top-actions">
                <button class="icon-button" aria-label="Notificações">♢<i></i></button>
                <div class="date-stamp">06 SET 2026</div>
            </div>
        </header>
        <section class="content">
            {#if error}
                <div class="alert error"><span>!</span>{error}
                    <button on:click={() => error = ''}>×</button>
                </div>
            {/if}
            {#if notice}
                <div class="alert success"><span>✓</span>{notice}
                    <button on:click={() => notice = ''}>×</button>
                </div>
            {/if}

            {#if currentPage === 'dashboard'}
                <div class="page-heading">
                    <div><p class="eyebrow">PAINEL DE OPERAÇÕES</p>
                        <h1>Visão geral</h1>
                        <p class="muted">Acompanhe a carteira e as principais movimentações do dia.</p></div>
                    <button class="button primary" on:click={newReceivable}>＋ Novo recebível</button>
                </div>
                <div class="metric-grid">
                    <div class="metric-card">
                        <div class="metric-icon blue">▤</div>
                        <div><span>Patrimônio em recebíveis</span><strong>{currency(faceTotal)}</strong><small
                                class="positive">↑ 8,4% <em>vs. mês anterior</em></small></div>
                    </div>
                    <div class="metric-card">
                        <div class="metric-icon violet">◈</div>
                        <div><span>Valor presente da carteira</span><strong>{currency(presentTotal)}</strong><small
                                class="positive">↑ 5,2% <em>vs. mês anterior</em></small></div>
                    </div>
                    <div class="metric-card">
                        <div class="metric-icon orange">▱</div>
                        <div><span>Fundos ativos</span><strong>{funds.length}</strong><small><em>fundos cadastrados</em></small>
                        </div>
                    </div>
                    <div class="metric-card">
                        <div class="metric-icon green">✓</div>
                        <div><span>Recebíveis ativos</span><strong>{receivables.length}</strong><small><em>na carteira
                            atual</em></small></div>
                    </div>
                </div>
                <div class="dashboard-grid">
                    <section class="panel">
                        <div class="panel-heading">
                            <div><h2>Últimos recebíveis</h2>
                                <p>Registros mais recentes da operação</p></div>
                            <a href="#/recebiveis">Ver todos →</a></div>
                        {#if loading}
                            <div class="empty">Carregando dados...</div>
                        {:else if receivables.length === 0}
                            <div class="empty">Nenhum recebível cadastrado.</div>
                        {:else}
                            <div class="table-wrap">
                                <table>
                                    <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Fundo</th>
                                        <th>Vencimento</th>
                                        <th>Valor de face</th>
                                        <th>Status</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {#each receivables.slice(-5).reverse() as item}
                                        <tr>
                                            <td class="mono">REC-{String(item.id).padStart(4, '0')}</td>
                                            <td><strong>{fundName(item)}</strong><small>{companyName(item)}</small></td>
                                            <td>{date(item.dataVencimento)}</td>
                                            <td>{currency(item.valorFace)}</td>
                                            <td><span class:warning={daysToMaturity(item.dataVencimento) <= 30}
                                                      class:danger={daysToMaturity(item.dataVencimento) < 0}
                                                      class="badge">{daysToMaturity(item.dataVencimento) < 0 ? 'Em atraso' : daysToMaturity(item.dataVencimento) <= 30 ? 'Vencendo' : 'Em carteira'}</span>
                                            </td>
                                        </tr>
                                    {/each}
                                    </tbody>
                                </table>
                            </div>
                        {/if}
                    </section>
                    <section class="panel">
                        <div class="panel-heading">
                            <div><h2>Fundos sob gestão</h2>
                                <p>Visão consolidada por fundo</p></div>
                            <a href="#/fundos">Ver todos →</a></div>
                        {#if funds.length === 0}
                            <div class="empty">Nenhum fundo cadastrado.</div>
                        {:else}
                            <div class="fund-list">
                                {#each funds.slice(0, 4) as fund}
                                    <div class="fund-row"><span
                                            class="fund-logo">{fund.nome?.slice(0, 2).toUpperCase()}</span>
                                        <div>
                                            <strong>{fund.nome}</strong><small>CNPJ {fund.cnpj || 'não informado'}</small>
                                        </div>
                                        <span class="fund-rate">{percent(fund.taxaBase)}<small>taxa base</small></span>
                                    </div>
                                {/each}
                            </div>
                        {/if}
                    </section>
                </div>
            {:else if currentPage === 'fundos'}
                <div class="page-heading">
                    <div><p class="eyebrow">GESTÃO DE CARTEIRAS</p>
                        <h1>Fundos</h1>
                        <p class="muted">Cadastre e consulte os fundos de investimento sob operação.</p></div>
                    <button class="button primary" on:click={newFund}>＋ Novo fundo</button>
                </div>
                <div class="toolbar">
                    <div class="search"><span>⌕</span><input bind:value={query} placeholder="Buscar por nome ou CNPJ"/>
                    </div>
                    <span class="result-count">{filteredFunds.length} fundos encontrados</span></div>
                <section class="panel">
                    <div class="table-wrap">
                        <table>
                            <thead>
                            <tr>
                                <th>Fundo</th>
                                <th>CNPJ</th>
                                <th>Taxa base</th>
                                <th>Conta vinculada</th>
                                <th></th>
                            </tr>
                            </thead>
                            <tbody>
                            {#each filteredFunds as fund}
                                <tr>
                                    <td>
                                        <div class="cell-main"><span
                                                class="fund-logo">{fund.nome?.slice(0, 2).toUpperCase()}</span><strong>{fund.nome}</strong>
                                        </div>
                                    </td>
                                    <td class="mono">{fund.cnpj || '—'}</td>
                                    <td>{percent(fund.taxaBase)}</td>
                                    <td>{fund.conta?.identificador || (fund.conta?.id ? `Conta #${fund.conta.id}` : '—')}</td>
                                    <td class="actions">
                                        <button on:click={() => editFund(fund)}>Editar</button>
                                        <button class="danger-link" on:click={() => removeFund(fund.id)}>Excluir
                                        </button>
                                    </td>
                                </tr>
                            {/each}
                            {#if !filteredFunds.length}
                                <tr>
                                    <td colspan="5">
                                        <div class="empty">Nenhum fundo encontrado.</div>
                                    </td>
                                </tr>
                            {/if}
                            </tbody>
                        </table>
                    </div>
                </section>
            {:else if currentPage === 'recebiveis'}
                <div class="page-heading">
                    <div><p class="eyebrow">ATIVOS DE CRÉDITO</p>
                        <h1>Recebíveis</h1>
                        <p class="muted">Controle, análise e acompanhamento dos direitos creditórios.</p></div>
                    <button class="button primary" on:click={newReceivable}>＋ Novo recebível</button>
                </div>
                <div class="toolbar">
                    <div class="search"><span>⌕</span><input bind:value={query}
                                                             placeholder="Buscar por fundo, empresa ou ID"/></div>
                    <div class="tabs">
                        <button class:active={activeTab === 'todos'} on:click={() => activeTab = 'todos'}>Todos
                            <b>{receivables.length}</b></button>
                        <button class:active={activeTab === 'vencendo'} on:click={() => activeTab = 'vencendo'}>
                            Vencendo
                        </button>
                        <button class:active={activeTab === 'atrasados'} on:click={() => activeTab = 'atrasados'}>Em
                            atraso
                        </button>
                    </div>
                </div>
                <section class="panel">
                    <div class="table-wrap">
                        <table>
                            <thead>
                            <tr>
                                <th>Identificação</th>
                                <th>Fundo</th>
                                <th>Empresa cedente</th>
                                <th>Vencimento</th>
                                <th>Valor de face</th>
                                <th>Valor presente</th>
                                <th></th>
                            </tr>
                            </thead>
                            <tbody>
                            {#each filteredReceivables as item}
                                <tr>
                                    <td class="mono">REC-{String(item.id).padStart(4, '0')}</td>
                                    <td><strong>{fundName(item)}</strong></td>
                                    <td>{companyName(item)}</td>
                                    <td><span
                                            class:date-warning={daysToMaturity(item.dataVencimento) <= 30}>{date(item.dataVencimento)}</span>
                                    </td>
                                    <td>{currency(item.valorFace)}</td>
                                    <td><strong>{currency(item.valorPresente)}</strong></td>
                                    <td class="actions">
                                        <button on:click={() => editReceivable(item)}>Editar</button>
                                        <button class="danger-link" on:click={() => removeReceivable(item.id)}>Excluir
                                        </button>
                                    </td>
                                </tr>
                            {/each}
                            {#if !filteredReceivables.length}
                                <tr>
                                    <td colspan="7">
                                        <div class="empty">Nenhum recebível encontrado.</div>
                                    </td>
                                </tr>
                            {/if}
                            </tbody>
                        </table>
                    </div>
                </section>
            {/if}
        </section>
    </main>
</div>

{#if showFundForm}
    <div class="modal-backdrop" role="presentation"
         on:click={(event) => event.target === event.currentTarget && closeForms()}>
        <section class="modal" role="dialog">
            <div class="modal-heading">
                <div><p class="eyebrow">CADASTRO DE FUNDO</p>
                    <h2>{editingFund ? 'Editar fundo' : 'Novo fundo'}</h2></div>
                <button class="close" on:click={closeForms}>×</button>
            </div>
            <form on:submit|preventDefault={saveFund}><label>Nome do fundo<input required bind:value={fundForm.nome}
                                                                                 placeholder="Ex.: SRM Crédito I FIDC"/></label><label>CNPJ<input
                    bind:value={fundForm.cnpj} placeholder="00.000.000/0000-00"/></label>
                <div class="form-grid"><label>Taxa base (decimal)<input required type="number" min="0" step="0.0001"
                                                                        bind:value={fundForm.taxaBase}
                                                                        placeholder="0,12"/></label><label>ID da conta
                    vinculada<input required type="number" min="1" bind:value={fundForm.contaId} placeholder="Ex.: 1"/></label>
                </div>
                <p class="form-hint">O backend associa o fundo à conta existente pelo <strong>contaId</strong>.</p>
                <div class="modal-actions">
                    <button type="button" class="button secondary" on:click={closeForms}>Cancelar</button>
                    <button class="button primary" type="submit">Salvar fundo</button>
                </div>
            </form>
        </section>
    </div>
{/if}

{#if showReceivableForm}
    <div class="modal-backdrop" role="presentation"
         on:click={(event) => event.target === event.currentTarget && closeForms()}>
        <section class="modal wide" role="dialog">
            <div class="modal-heading">
                <div><p class="eyebrow">ATIVO DE CRÉDITO</p>
                    <h2>{editingReceivable ? 'Editar recebível' : 'Novo recebível'}</h2></div>
                <button class="close" on:click={closeForms}>×</button>
            </div>
            <form on:submit|preventDefault={saveReceivable}>
                <div class="form-grid"><label>Valor de face (R$)<input required type="number" min="0" step="0.01"
                                                                       bind:value={receivableForm.valorFace}
                                                                       placeholder="0,00"/></label><label>Data de
                    vencimento<input required type="date" bind:value={receivableForm.dataVencimento}/></label><label>Fundo<select
                        required bind:value={receivableForm.fundoId}>
                    <option value="">Selecione o fundo</option>
                    {#each funds as fund}
                        <option value={fund.id}>{fund.nome}</option>
                    {/each}
                </select></label><label>Empresa cedente<select required bind:value={receivableForm.empresaId}>
                    <option value="">Selecione a empresa</option>
                    {#each companies as company}
                        <option value={company.id}>{company.razaoSocial}</option>
                    {/each}
                </select></label> <label>ID do tipo de recebível<input required type="number" min="1"
                                                                       bind:value={receivableForm.tipoId}
                                                                       placeholder="Ex.: 1"/></label><label>Spread
                    (decimal)<input type="number" min="0" step="0.0001" bind:value={receivableForm.spread}
                                    placeholder="0,02"/></label><label>Taxa base (decimal)<input type="number" min="0"
                                                                                                 step="0.0001"
                                                                                                 bind:value={receivableForm.taxaBase}
                                                                                                 placeholder="0,12"/></label><label>Prazo
                    em anos<input type="number" min="0" step="0.0001" bind:value={receivableForm.prazo}
                                  placeholder="Calculado pela data"/></label></div>
                <div class="calculation">
                    <span><small>PRÉVIA DO VALOR PRESENTE</small><strong>{currency(calculatedPresentValue)}</strong></span><span><small>DIAS ATÉ O VENCIMENTO</small><strong>{Math.max(daysToMaturity(receivableForm.dataVencimento), 0)}
                    dias</strong></span><span><small>TAXA TOTAL</small><strong>{percent(Number(receivableForm.taxaBase || 0) + Number(receivableForm.spread || 0))}</strong></span>
                </div>
                <p class="form-hint">O valor presente final é recalculado pelo backend com a taxa do fundo e o spread do
                    tipo.</p>
                <div class="modal-actions">
                    <button type="button" class="button secondary" on:click={closeForms}>Cancelar</button>
                    <button class="button primary" type="submit">Salvar recebível</button>
                </div>
            </form>
        </section>
    </div>
{/if}
