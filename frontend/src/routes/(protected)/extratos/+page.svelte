<script lang="ts">
    import {onMount} from 'svelte';
    import {api} from '$lib/api';
    import type {Company, Currency, Fund, StatementFilters, StatementTransaction} from '$lib/types';

    let funds = $state<Fund[]>([]);
    let companies = $state<Company[]>([]);
    let currencies = $state<Currency[]>([]);
    let transactions = $state<StatementTransaction[]>([]);
    let loading = $state(false);
    let loadingOptions = $state(true);
    let error = $state('');
    let notice = $state('');
    let filters = $state<{
        dataInicial: string;
        dataFinal: string;
        fundoId: string;
        empresaId: string;
        moedaId: string;
    }>({
        dataInicial: '',
        dataFinal: '',
        fundoId: '',
        empresaId: '',
        moedaId: ''
    });

    const currency = (value: number | null | undefined) => new Intl.NumberFormat('pt-BR', {
        style: 'currency',
        currency: 'BRL'
    }).format(Number(value || 0));
    const date = (value: string | null | undefined) => value
        ? new Intl.DateTimeFormat('pt-BR', {dateStyle: 'short', timeStyle: 'short'}).format(new Date(value))
        : '—';

    onMount(async () => {
        try {
            [funds, companies, currencies] = await Promise.all([
                api.funds(),
                api.companies(),
                api.currencies()
            ]);
        } catch (err: unknown) {
            error = err instanceof Error ? err.message : 'Não foi possível carregar os filtros.';
        } finally {
            loadingOptions = false;
        }
    });

    function currentFilters(): StatementFilters {
        return {
            dataInicial: filters.dataInicial || undefined,
            dataFinal: filters.dataFinal || undefined,
            fundoId: filters.fundoId ? Number(filters.fundoId) : undefined,
            empresaId: filters.empresaId ? Number(filters.empresaId) : undefined,
            moedaId: filters.moedaId ? Number(filters.moedaId) : undefined
        };
    }

    async function search() {
        error = '';
        notice = '';
        if (filters.dataInicial && filters.dataFinal && filters.dataInicial > filters.dataFinal) {
            error = 'A data inicial não pode ser posterior à data final.';
            return;
        }
        loading = true;
        try {
            transactions = await api.statement(currentFilters());
        } catch (err: unknown) {
            error = err instanceof Error ? err.message : 'Não foi possível consultar o extrato.';
        } finally {
            loading = false;
        }
    }

    async function download(format: 'csv' | 'parquet') {
        error = '';
        notice = '';
        if (filters.dataInicial && filters.dataFinal && filters.dataInicial > filters.dataFinal) {
            error = 'A data inicial não pode ser posterior à data final.';
            return;
        }
        try {
            const blob = await api.downloadStatement(currentFilters(), format);
            const url = URL.createObjectURL(blob);
            const anchor = document.createElement('a');
            anchor.href = url;
            anchor.download = `extrato.${format}`;
            anchor.click();
            URL.revokeObjectURL(url);
            notice = `Extrato ${format.toUpperCase()} gerado com sucesso.`;
        } catch (err: unknown) {
            error = err instanceof Error ? err.message : 'Não foi possível gerar o extrato.';
        }
    }

    function reset() {
        filters = {dataInicial: '', dataFinal: '', fundoId: '', empresaId: '', moedaId: ''};
        transactions = [];
        error = '';
        notice = '';
    }
</script>

<svelte:head><title>Extratos | Operator Portal</title></svelte:head>

{#if error}<div class="alert error"><span>!</span>{error}</div>{/if}
{#if notice}<div class="alert success"><span>✓</span>{notice}</div>{/if}

<div class="page-heading">
    <div>
        <p class="eyebrow">MOVIMENTAÇÕES FINANCEIRAS</p>
        <h1>Extratos</h1>
        <p class="muted">Consulte e extraia os lançamentos conforme os filtros informados.</p>
    </div>
</div>

<section class="panel statement-filters">
    <div class="panel-heading">
        <div><h2>Parâmetros do extrato</h2><p>Deixe um campo vazio para não aplicar o filtro.</p></div>
    </div>
    <form onsubmit={(event) => { event.preventDefault(); search(); }}>
        <div class="form-grid">
            <label>Data inicial<input type="date" bind:value={filters.dataInicial}/></label>
            <label>Data final<input type="date" bind:value={filters.dataFinal}/></label>
            <label>Fundo
                <select bind:value={filters.fundoId} disabled={loadingOptions}>
                    <option value="">Todos os fundos</option>
                    {#each funds as fund}<option value={fund.id}>{fund.nome}</option>{/each}
                </select>
            </label>
            <label>Empresa
                <select bind:value={filters.empresaId} disabled={loadingOptions}>
                    <option value="">Todas as empresas</option>
                    {#each companies as company}<option value={company.id}>{company.razaoSocial}</option>{/each}
                </select>
            </label>
            <label>Moeda
                <select bind:value={filters.moedaId} disabled={loadingOptions}>
                    <option value="">Todas as moedas</option>
                    {#each currencies as currencyOption}<option value={currencyOption.id}>{currencyOption.codigo} — {currencyOption.nome}</option>{/each}
                </select>
            </label>
        </div>
        <div class="modal-actions">
            <button type="button" class="button secondary" onclick={reset}>Limpar</button>
            <button type="submit" class="button primary" disabled={loading}>{loading ? 'Consultando...' : 'Consultar extrato'}</button>
        </div>
    </form>
</section>

<section class="panel statement-results">
    <div class="panel-heading">
        <div><h2>Resultado</h2><p>{transactions.length} transações encontradas</p></div>
        <div class="statement-actions">
            <button class="button secondary" onclick={() => download('csv')}>Exportar CSV</button>
            <button class="button secondary" onclick={() => download('parquet')}>Exportar Parquet</button>
        </div>
    </div>
    {#if loading}
        <div class="empty">Consultando transações...</div>
    {:else if !transactions.length}
        <div class="empty">Informe os filtros e consulte o extrato para visualizar as transações.</div>
    {:else}
        <div class="table-wrap">
            <table>
                <thead><tr><th>ID</th><th>Data</th><th>Valor</th><th>Status</th><th>Conta origem</th><th>Conta destino</th></tr></thead>
                <tbody>
                    {#each transactions as transaction}
                        <tr>
                            <td class="mono">TRX-{String(transaction.id).padStart(4, '0')}</td>
                            <td>{date(transaction.realizadaEm)}</td>
                            <td><strong>{currency(transaction.valor)}</strong></td>
                            <td><span class="badge" class:danger={transaction.status === 'FALHA'} class:warning={transaction.status === 'PENDENTE'}>{transaction.status}</span></td>
                            <td>{transaction.contaOrigem?.identificador || `Conta #${transaction.contaOrigem?.id ?? '—'}`}</td>
                            <td>{transaction.contaDestino?.identificador || `Conta #${transaction.contaDestino?.id ?? '—'}`}</td>
                        </tr>
                    {/each}
                </tbody>
            </table>
        </div>
    {/if}
</section>
