<script lang="ts">
    import {onMount} from 'svelte';
    import {api} from '$lib/api';
    import type {Company, Fund, Receivable, ReceivableType, UserAccess} from '$lib/types';
    import ReceivableModal from '$lib/components/ReceivableModal.svelte';

    let funds = $state<Fund[]>([]);
    let companies = $state<Company[]>([]);
    let receivableTypes = $state<ReceivableType[]>([]);
    let receivables = $state<Receivable[]>([]);
    let query = $state('');
    let activeTab = $state('todos');
    let selected = $state<Receivable | null | undefined>(undefined);
    let error = $state('');
    let notice = $state('');
    let isAdmin = $state(false);
    type SortKey = 'id' | 'fund' | 'company' | 'dueDate' | 'faceValue' | 'baseRate';
    let sortKey = $state<SortKey>('id');
    let sortDirection = $state<'asc' | 'desc'>('asc');
    const currency = (value: number | null | undefined) => new Intl.NumberFormat('pt-BR', {
        style: 'currency',
        currency: 'BRL'
    }).format(Number(value || 0));
    const days = (value: string | null | undefined) => value ? Math.ceil((new Date(`${value}T00:00:00`).getTime() - Date.now()) / 86400000) : 0;
    const fundName = (item: Receivable) => item?.fundo?.nome || funds.find((fund) => String(fund.id) === String(item.fundoId))?.nome || '—';
    const companyName = (item: Receivable) => item?.empresa?.razaoSocial || companies.find((company) => String(company.id) === String(item.empresaId))?.razaoSocial || '—';
    let filtered = $derived.by(() => {
        const result = receivables.filter((item) => {
            const matchesTab = activeTab === 'todos'
                || (activeTab === 'disponiveis' && !item.fundoId)
                || (activeTab === 'vencendo' && days(item.dataVencimento) <= 30)
                || (activeTab === 'atrasados' && days(item.dataVencimento) < 0);
            return matchesTab && `${item.id} ${fundName(item)} ${companyName(item)}`.toLowerCase().includes(query.toLowerCase());
        });
        return result.sort((left, right) => {
            const values: Record<SortKey, [string | number, string | number]> = {
                id: [left.id, right.id],
                fund: [fundName(left), fundName(right)],
                company: [companyName(left), companyName(right)],
                dueDate: [left.dataVencimento || '', right.dataVencimento || ''],
                faceValue: [Number(left.valorFace || 0), Number(right.valorFace || 0)],
                baseRate: [Number(left.taxaBase || 0), Number(right.taxaBase || 0)]
            };
            const [leftValue, rightValue] = values[sortKey];
            const comparison = typeof leftValue === 'string' && typeof rightValue === 'string'
                ? leftValue.localeCompare(rightValue, 'pt-BR')
                : Number(leftValue) - Number(rightValue);
            return sortDirection === 'asc' ? comparison : -comparison;
        });
    });

    function sortBy(key: SortKey) {
        if (sortKey === key) {
            sortDirection = sortDirection === 'asc' ? 'desc' : 'asc';
        } else {
            sortKey = key;
            sortDirection = 'asc';
        }
    }

    function sortIndicator(key: SortKey) {
        return sortKey === key ? (sortDirection === 'asc' ? '↑' : '↓') : '↕';
    }
    onMount(async () => {
        try {
            let profile: UserAccess;
            [funds, receivables, companies, receivableTypes, profile] = await Promise.all([
                api.funds(), api.receivables(), api.companies(), api.receivableTypes(), api.currentUser()
            ]);
            isAdmin = profile.tipo.toUpperCase() === 'ADMIN';
        } catch (err: unknown) {
            error = err instanceof Error ? err.message : 'Não foi possível carregar os recebíveis.';
        }
    });

    async function remove(id: number) {
        if (!confirm('Excluir este recebível?')) return;
        try {
            await api.deleteReceivable(id);
            receivables = receivables.filter((item) => item.id !== id);
            notice = 'Recebível removido.';
        } catch (err: unknown) {
            error = err instanceof Error ? err.message : 'Não foi possível excluir o recebível.';
        }
    }
</script>

<svelte:head><title>Recebíveis | Operator Portal</title></svelte:head>
{#if error}
    <div class="alert error">{error}</div>
{/if}
{#if notice}
    <div class="alert success">{notice}</div>
{/if}
<div class="page-heading">
    <div><p class="eyebrow">ATIVOS DE CRÉDITO</p>
        <h1>Recebíveis</h1>
        <p class="muted">Controle, análise e acompanhamento dos direitos creditórios.</p></div>
    <button class="button primary" onclick={() => selected = null}>＋ Novo recebível</button>
</div>
<div class="toolbar">
    <div class="search"><span>⌕</span><input bind:value={query} placeholder="Buscar por fundo, empresa ou ID"/></div>
    <div class="tabs">
        <button class:active={activeTab === 'todos'} onclick={() => activeTab = 'todos'}>Todos
            <b>{receivables.length}</b></button>
        <button class:active={activeTab === 'disponiveis'} onclick={() => activeTab = 'disponiveis'}>Disponíveis para compra</button>
        <button class:active={activeTab === 'vencendo'} onclick={() => activeTab = 'vencendo'}>Vencendo</button>
        <button class:active={activeTab === 'atrasados'} onclick={() => activeTab = 'atrasados'}>Em atraso</button>
    </div>
</div>
<section class="panel">
    <div class="table-wrap">
        <table>
            <thead>
            <tr>
                <th><button class="sort-button" onclick={() => sortBy('id')}>Identificação {sortIndicator('id')}</button></th>
                <th><button class="sort-button" onclick={() => sortBy('fund')}>Fundo {sortIndicator('fund')}</button></th>
                <th><button class="sort-button" onclick={() => sortBy('company')}>Empresa cedente {sortIndicator('company')}</button></th>
                <th><button class="sort-button" onclick={() => sortBy('dueDate')}>Vencimento {sortIndicator('dueDate')}</button></th>
                <th><button class="sort-button" onclick={() => sortBy('faceValue')}>Valor de face {sortIndicator('faceValue')}</button></th>
                <th><button class="sort-button" onclick={() => sortBy('baseRate')}>Taxa base {sortIndicator('baseRate')}</button></th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            {#each filtered as item}
                <tr>
                    <td class="mono">REC-{String(item.id).padStart(4, '0')}</td>
                    <td><strong>{fundName(item)}</strong></td>
                    <td>{companyName(item)}</td>
                    <td class:date-warning={days(item.dataVencimento) <= 30}>{new Intl.DateTimeFormat('pt-BR').format(new Date(`${item.dataVencimento}T00:00:00`))}</td>
                    <td>{currency(item.valorFace)}</td>
                    <td>{(Number(item.taxaBase || 0) * 100).toFixed(2)}%</td>
                    <td class="actions">
                        <a href={`/recebiveis/${item.id}`}>Ver</a>
                        {#if isAdmin}<button onclick={() => selected = item}>Editar</button>
                            <button class="danger-link" onclick={() => remove(item.id)}>Excluir</button>{/if}
                    </td>
                </tr>
            {/each}
            {#if !filtered.length}
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
{#if selected !== undefined}
    <ReceivableModal item={selected} {funds} {companies} {receivableTypes} {isAdmin} onclose={() => selected = undefined}
                     onsaved={(saved: Receivable) => { receivables = selected ? receivables.map((item) => item.id === saved.id ? saved : item) : [...receivables, saved]; selected = undefined; notice = 'Recebível salvo com sucesso.'; }}/>
{/if}
