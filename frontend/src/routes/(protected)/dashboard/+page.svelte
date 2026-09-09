<script lang="ts">
    import {onMount} from 'svelte';
    import {api} from '$lib/api';
    import type {Fund, Receivable} from '$lib/types';

    let funds = $state<Fund[]>([]);
    let receivables = $state<Receivable[]>([]);
    let loading = $state(true);
    let error = $state('');
    const currency = (value: number | null | undefined) => new Intl.NumberFormat('pt-BR', {
        style: 'currency',
        currency: 'BRL'
    }).format(Number(value || 0));
    const date = (value: string | null | undefined) => value ? new Intl.DateTimeFormat('pt-BR').format(new Date(`${value}T00:00:00`)) : '—';
    const daysToMaturity = (value: string | null | undefined) => value ? Math.ceil((new Date(`${value}T00:00:00`).getTime() - Date.now()) / 86400000) : 0;

    onMount(async () => {
        try {
            [funds, receivables] = await Promise.all([api.funds(), api.receivables()]);
        } catch (err: unknown) {
            error = err instanceof Error ? err.message : 'Não foi possível carregar o dashboard.';
        } finally {
            loading = false;
        }
    });
</script>

<svelte:head><title>Visão geral | Operator Portal</title></svelte:head>

{#if error}
    <div class="alert error"><span>!</span>{error}</div>
{/if}
<div class="page-heading">
    <div><p class="eyebrow">PAINEL DE OPERAÇÕES</p>
        <h1>Visão geral</h1>
        <p class="muted">Acompanhe a carteira e as principais movimentações do dia.</p></div>
    <a class="button primary" href="/recebiveis">＋ Novo recebível</a></div>
<div class="metric-grid">
    <div class="metric-card">
        <div class="metric-icon blue">▤</div>
        <div>
            <span>Patrimônio em recebíveis</span><strong>{currency(receivables.reduce((sum, item) => sum + Number(item.valorFace || 0), 0))}</strong><small
                class="positive">Carteira atual</small></div>
    </div>
    <div class="metric-card">
        <div class="metric-icon violet">◈</div>
        <div>
            <span>Valor presente da carteira</span><strong>{currency(receivables.reduce((sum, item) => sum + Number(item.valorPresente || 0), 0))}</strong><small
                class="positive">Valor atualizado</small></div>
    </div>
    <div class="metric-card">
        <div class="metric-icon orange">▱</div>
        <div><span>Fundos ativos</span><strong>{funds.length}</strong><small>fundos cadastrados</small></div>
    </div>
    <div class="metric-card">
        <div class="metric-icon green">✓</div>
        <div><span>Recebíveis ativos</span><strong>{receivables.length}</strong><small>na carteira atual</small></div>
    </div>
</div>
<div class="dashboard-grid">
    <section class="panel">
        <div class="panel-heading">
            <div><h2>Últimos recebíveis</h2>
                <p>Registros mais recentes da operação</p></div>
            <a href="/recebiveis">Ver todos →</a></div>
        {#if loading}
            <div class="empty">Carregando dados...</div>
        {:else if !receivables.length}
            <div class="empty">Nenhum recebível cadastrado.</div>
        {:else}
            <div class="table-wrap">
                <table>
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Vencimento</th>
                        <th>Valor de face</th>
                        <th>Status</th>
                    </tr>
                    </thead>
                    <tbody>
                    {#each receivables.slice(-5).reverse() as item}
                        <tr>
                            <td class="mono">REC-{String(item.id).padStart(4, '0')}</td>
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
            <a href="/fundos">Ver todos →</a></div>
        {#if !funds.length}
            <div class="empty">Nenhum fundo cadastrado.</div>
        {:else}
            <div class="fund-list">
                {#each funds.slice(0, 4) as fund}
                    <div class="fund-row"><span class="fund-logo">{fund.nome?.slice(0, 2).toUpperCase()}</span>
                        <div><strong>{fund.nome}</strong><small>CNPJ {fund.cnpj || 'não informado'}</small></div>
                    </div>
                {/each}
            </div>
        {/if}
    </section>
</div>
