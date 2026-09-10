<script lang="ts">
    import {onMount} from 'svelte';
    import {api} from '$lib/api';
    import type {Fund} from '$lib/types';
    import FundModal from '$lib/components/FundModal.svelte';

    let funds = $state<Fund[]>([]);
    let query = $state('');
    let error = $state('');
    let notice = $state('');
    let isAdmin = $state(false);
    let selected = $state<Fund | null | undefined>(undefined);
    let filtered = $derived(funds.filter((fund) => `${fund.nome} ${fund.cnpj}`.toLowerCase().includes(query.toLowerCase())));
    onMount(load);

    async function load() {
        try {
            const [loadedFunds, profile] = await Promise.all([api.funds(), api.currentUser()]);
            funds = loadedFunds;
            isAdmin = profile.tipo.toUpperCase() === 'ADMIN';
        } catch (err: unknown) {
            error = err instanceof Error ? err.message : 'Não foi possível carregar os fundos.';
        }
    }

    async function remove(id: number) {
        if (!confirm('Excluir este fundo?')) return;
        try {
            await api.deleteFund(id);
            funds = funds.filter((fund) => fund.id !== id);
            notice = 'Fundo removido.';
        } catch (err: unknown) {
            error = err instanceof Error ? err.message : 'Não foi possível excluir o fundo.';
        }
    }
</script>

<svelte:head><title>Fundos | Operator Portal</title></svelte:head>
{#if error}
    <div class="alert error">{error}</div>
{/if}
{#if notice}
    <div class="alert success">{notice}</div>
{/if}
<div class="page-heading">
    <div><p class="eyebrow">GESTÃO DE CARTEIRAS</p>
        <h1>Fundos</h1>
        <p class="muted">Cadastre e consulte os fundos de investimento sob operação.</p></div>
    {#if isAdmin}<button class="button primary" onclick={() => selected = null}>＋ Novo fundo</button>{/if}
</div>
<div class="toolbar">
    <div class="search"><span>⌕</span><input bind:value={query} placeholder="Buscar por nome ou CNPJ"/></div>
    <span class="result-count">{filtered.length} fundos encontrados</span></div>
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
            {#each filtered as fund}
                <tr>
                    <td>
                        <div class="cell-main"><span
                                class="fund-logo">{fund.nome?.slice(0, 2).toUpperCase()}</span><a href={`/fundos/${fund.id}`}><strong>{fund.nome}</strong></a>
                        </div>
                    </td>
                    <td class="mono">{fund.cnpj || '—'}</td>
                    <td>{(Number(fund.taxaBase || 0) * 100).toFixed(2)}%</td>
                    <td>{fund.conta?.identificador || '—'}</td>
                    <td class="actions">
                        {#if isAdmin}<button onclick={() => selected = fund}>Editar</button>{/if}
                        {#if isAdmin}<button class="danger-link" onclick={() => remove(fund.id)}>Excluir</button>{/if}
                    </td>
                </tr>
            {/each}
            {#if !filtered.length}
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
{#if selected !== undefined}
    <FundModal fund={selected} onclose={() => selected = undefined}
               onsaved={(saved: Fund) => { funds = selected ? funds.map((fund) => fund.id === saved.id ? saved : fund) : [...funds, saved]; selected = undefined; notice = 'Fundo salvo com sucesso.'; }}/>
{/if}
