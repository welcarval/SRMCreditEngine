<script lang="ts">
    import {onMount} from 'svelte';
    import {page} from '$app/state';
    import {api} from '$lib/api';
    import ReceivableModal from '$lib/components/ReceivableModal.svelte';
    import type {Company, Fund, Receivable, ReceivableType} from '$lib/types';

    let fund = $state<Fund | null>(null);
    let funds = $state<Fund[]>([]);
    let companies = $state<Company[]>([]);
    let receivableTypes = $state<ReceivableType[]>([]);
    let receivables = $state<Receivable[]>([]);
    let selected = $state<Receivable | null | undefined>(undefined);
    let loading = $state(true);
    let error = $state('');
    let notice = $state('');
    const fundId = $derived(Number(page.params.id));
    const currency = (value: number | null | undefined) => new Intl.NumberFormat('pt-BR', {
        style: 'currency',
        currency: 'BRL'
    }).format(Number(value || 0));
    const date = (value: string | null | undefined) => value
        ? new Intl.DateTimeFormat('pt-BR').format(new Date(`${value}T00:00:00`))
        : '—';
    const fundReceivables = $derived(receivables.filter((item) => Number(item.fundo?.id ?? item.fundoId) === fundId));

    onMount(async () => {
        try {
            [funds, receivables, companies, receivableTypes] = await Promise.all([
                api.funds(), api.receivables(), api.companies(), api.receivableTypes()
            ]);
            fund = funds.find((item) => item.id === fundId) || null;
            if (!fund) error = 'Fundo não encontrado.';
        } catch (err: unknown) {
            error = err instanceof Error ? err.message : 'Não foi possível carregar o fundo.';
        } finally {
            loading = false;
        }
    });
</script>

<svelte:head><title>{fund ? `${fund.nome} | Fundos` : 'Fundo | Operator Portal'}</title></svelte:head>

{#if error}<div class="alert error">{error}</div>{/if}
{#if notice}<div class="alert success">{notice}</div>{/if}
{#if loading}
    <div class="empty">Carregando fundo...</div>
{:else if fund}
    <div class="page-heading">
        <div>
            <p class="eyebrow">DETALHES DA CARTEIRA</p>
            <h1>{fund.nome}</h1>
            <p class="muted">CNPJ {fund.cnpj || 'não informado'} · Taxa base {(Number(fund.taxaBase || 0) * 100).toFixed(2)}%</p>
        </div>
        <button class="button primary" onclick={() => selected = null}>＋ Comprar recebível</button>
    </div>
    <div class="metric-grid">
        <div class="metric-card"><div class="metric-icon blue">▤</div><div><span>Recebíveis no fundo</span><strong>{fundReceivables.length}</strong><small>ativos vinculados</small></div></div>
        <div class="metric-card"><div class="metric-icon green">✓</div><div><span>Valor de face</span><strong>{currency(fundReceivables.reduce((sum, item) => sum + Number(item.valorFace || 0), 0))}</strong><small>carteira do fundo</small></div></div>
    </div>
    <section class="panel">
        <div class="panel-heading"><div><h2>Recebíveis do fundo</h2><p>Ativos adquiridos para esta carteira</p></div></div>
        {#if !fundReceivables.length}
            <div class="empty">Nenhum recebível vinculado. Compre o primeiro recebível para este fundo.</div>
        {:else}
            <div class="table-wrap"><table><thead><tr><th>Identificação</th><th>Vencimento</th><th>Valor de face</th><th>Valor presente</th></tr></thead><tbody>
                {#each fundReceivables as item}
                    <tr><td class="mono">REC-{String(item.id).padStart(4, '0')}</td><td>{date(item.dataVencimento)}</td><td>{currency(item.valorFace)}</td><td><strong>{currency(item.valorPresente)}</strong></td></tr>
                {/each}
            </tbody></table></div>
        {/if}
    </section>
    {#if selected !== undefined}
        <ReceivableModal item={selected} {funds} {companies} {receivableTypes} fundId={fund.id}
                         onclose={() => selected = undefined}
                         onsaved={(saved: Receivable) => { receivables = [...receivables, saved]; selected = undefined; notice = 'Recebível comprado com sucesso.'; }}/>
    {/if}
{:else}
    <div class="empty">Fundo não encontrado.</div>
{/if}
