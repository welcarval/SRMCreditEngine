<script lang="ts">
    import {onMount} from 'svelte';
    import {page} from '$app/state';
    import {api} from '$lib/api';
    import type {Fund, Receivable} from '$lib/types';

    let receivable = $state<Receivable | null>(null);
    let funds = $state<Fund[]>([]);
    let selectedFundId = $state<number | null>(null);
    let loading = $state(true);
    let error = $state('');
    let notice = $state('');
    let buying = $state(false);
    const currency = (value: number | null | undefined) => new Intl.NumberFormat('pt-BR', {
        style: 'currency', currency: 'BRL'
    }).format(Number(value || 0));
    const selectedFund = $derived(funds.find((fund) => fund.id === selectedFundId) ?? null);
    const presentValue = $derived(receivable && receivable.fundoId
        ? Number(receivable.valorPresente || 0)
        : receivable && selectedFund
            ? Number(receivable.valorFace || 0) / Math.pow(
            1 + Number(selectedFund.taxaBase || 0) + Number(receivable.spread || 0),
            Math.max((new Date(`${receivable.dataVencimento}T00:00:00`).getTime() - Date.now()) / 86400000 / 365, 0))
            : null);
    const discount = $derived(presentValue === null || !receivable
        ? null
        : Math.max(Number(receivable.valorFace || 0) - presentValue, 0));
    const discountRate = $derived(discount === null || !receivable || !Number(receivable.valorFace)
        ? null : discount / Number(receivable.valorFace));

    onMount(async () => {
        try {
            const [receivables, loadedFunds] = await Promise.all([api.receivables(), api.funds()]);
            receivable = receivables.find((item) => item.id === Number(page.params.id)) ?? null;
            funds = loadedFunds;
            if (!receivable) error = 'Recebível não encontrado.';
            else if (!receivable.fundoId) selectedFundId = funds[0]?.id ?? null;
        } catch (err: unknown) {
            error = err instanceof Error ? err.message : 'Não foi possível carregar o recebível.';
        } finally {
            loading = false;
        }
    });

    async function buy() {
        if (!receivable || !selectedFund || buying) return;
        if (!confirm(`Confirma a compra deste recebível para o fundo ${selectedFund.nome}?`)) return;
        buying = true;
        try {
            receivable = await api.addReceivableToFund(selectedFund.id, receivable.id);
            funds = await api.funds();
            notice = 'Recebível comprado com sucesso.';
        } catch (err: unknown) {
            error = err instanceof Error ? err.message : 'Não foi possível comprar o recebível.';
        } finally {
            buying = false;
        }
    }
</script>

<svelte:head>
    <title>{receivable ? `REC-${String(receivable.id).padStart(4, '0')}` : 'Recebível'} | Operator Portal</title>
</svelte:head>

{#if error}
    <div class="alert error">{error}</div>
{/if}
{#if notice}
    <div class="alert success">{notice}</div>
{/if}
{#if loading}
    <div class="empty">Carregando recebível...</div>
{:else if receivable}
    <div class="page-heading">
        <div><a href="/recebiveis">← Voltar para recebíveis</a>
            <p class="eyebrow">DETALHES DO ATIVO</p>
            <h1>REC-{String(receivable.id).padStart(4, '0')}</h1>
            <p class="muted">{receivable.fundoId ? 'Recebível associado a um fundo' : 'Recebível sem fundo associado'}</p>
        </div>
    </div>
    <div class="metric-grid">
        <div class="metric-card">
            <div class="metric-icon blue">◈</div>
            <div>
                <span>Valor presente</span>
                <strong>{presentValue === null ? '—' : currency(presentValue)}</strong>
                <small>{receivable.fundoId ? 'valor de aquisição' : 'estimado para o fundo selecionado'}</small>
            </div>
        </div>
        <div class="metric-card">
            <div class="metric-icon orange">−</div>
            <div>
                <span>Deságio</span>
                <strong>{discount === null ? '—' : currency(discount)}</strong>
                <small>
                    {discountRate === null ? 'selecione um fundo' : `${(discountRate * 100).toFixed(2)}% do valor de face`}
                </small>
            </div>
        </div>
        {#if selectedFund}
            <div class="metric-card">
                <div class="metric-icon green">$</div>
                <div>
                    <span>Saldo do fundo</span>
                    <strong>{currency(selectedFund.saldo)}</strong>
                    <small>{selectedFund.nome}</small>
                </div>
            </div>
        {/if}
    </div>
    <section class="panel">
        <div class="panel-heading">
            <div>
                <h2>Informações do recebível</h2>
                <p>Valor de face: {currency(receivable.valorFace)}</p>
            </div>
        </div>
        {#if !receivable.fundoId}
            <div class="purchase-panel">
                <label>Comprar para o fundo
                    <select bind:value={selectedFundId}>
                        <option value={null}>Selecione um fundo</option>
                        {#each funds as fund}
                            <option value={fund.id}>{fund.nome} — saldo {currency(fund.saldo)}</option>
                        {/each}
                    </select>
                </label>
                <button class="button primary" disabled={!selectedFundId || buying}
                        onclick={buy}>{buying ? 'Comprando...' : 'Comprar recebível'}</button>
            </div>
        {:else}
            <div class="empty">Este recebível já está associado a um fundo.</div>
        {/if}
    </section>
{:else}
    <div class="empty">Recebível não encontrado.</div>
{/if}
