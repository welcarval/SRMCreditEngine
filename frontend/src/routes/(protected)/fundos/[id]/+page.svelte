<script lang="ts">
    import {onMount} from 'svelte';
    import {page} from '$app/state';
    import {api} from '$lib/api';
    import {hasScope} from '$lib/permissions';
    import ReceivableModal from '$lib/components/ReceivableModal.svelte';
    import type {Company, Fund, Receivable, ReceivableType, UserAccess} from '$lib/types';

    let fund = $state<Fund | null>(null);
    let funds = $state<Fund[]>([]);
    let companies = $state<Company[]>([]);
    let receivableTypes = $state<ReceivableType[]>([]);
    let receivables = $state<Receivable[]>([]);
    let selected = $state<Receivable | null | undefined>(undefined);
    let loading = $state(true);
    let error = $state('');
    let notice = $state('');
    let canCreateReceivables = $state(false);
    let canBuyReceivables = $state(false);
    let canManageFunds = $state(false);
    let buyOpen = $state(false);
    const fundId = $derived(Number(page.params.id));
    const currency = (value: number | null | undefined) => new Intl.NumberFormat('pt-BR', {
        style: 'currency',
        currency: 'BRL'
    }).format(Number(value || 0));
    const date = (value: string | null | undefined) => value
        ? new Intl.DateTimeFormat('pt-BR').format(new Date(`${value}T00:00:00`))
        : '—';
    const fundReceivables = $derived(receivables.filter((item) => Number(item.fundo?.id ?? item.fundoId) === fundId));
    const availableReceivables = $derived(receivables.filter((item) => !item.fundo && !item.fundoId));

    onMount(async () => {
        try {
            let profile: UserAccess;
            [funds, receivables, companies, receivableTypes, profile] = await Promise.all([
                api.funds(), api.receivables(), api.companies(), api.receivableTypes(), api.currentUser()
            ]);
            canCreateReceivables = hasScope(profile, 'recebiveis:criar');
            canBuyReceivables = hasScope(profile, 'recebiveis:comprar');
            canManageFunds = hasScope(profile, 'fundos:gerenciar');
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
        {#if canBuyReceivables}<button class="button primary" onclick={() => buyOpen = true}>＋ Comprar recebível</button>{/if}
    </div>
    <div class="metric-grid">
        <div class="metric-card"><div class="metric-icon blue">▤</div><div><span>Recebíveis no fundo</span><strong>{fundReceivables.length}</strong><small>ativos vinculados</small></div></div>
        <div class="metric-card"><div class="metric-icon green">✓</div><div><span>Valor de face</span><strong>{currency(fundReceivables.reduce((sum, item) => sum + Number(item.valorFace || 0), 0))}</strong><small>carteira do fundo</small></div></div>
        <div class="metric-card"><div class="metric-icon violet">$</div><div><span>Saldo da conta</span><strong>{currency(fund.saldo)}</strong><small>disponível para compras</small></div></div>
    </div>
    <section class="panel">
        <div class="panel-heading"><div><h2>Recebíveis do fundo</h2><p>Ativos adquiridos para esta carteira</p></div></div>
        {#if !fundReceivables.length}
            <div class="empty">Nenhum recebível vinculado. Compre o primeiro recebível para este fundo.</div>
        {:else}
            <div class="table-wrap"><table><thead><tr><th>Identificação</th><th>Vencimento</th><th>Valor de face</th><th>Valor presente</th></tr></thead><tbody>
                {#each fundReceivables as item}
                    <tr><td class="mono"><a href={`/recebiveis/${item.id}`}>REC-{String(item.id).padStart(4, '0')}</a></td><td>{date(item.dataVencimento)}</td><td>{currency(item.valorFace)}</td><td><strong>{currency(item.valorPresente)}</strong></td></tr>
                {/each}
            </tbody></table></div>
        {/if}
    </section>
    {#if selected !== undefined}
        <ReceivableModal item={selected} {funds} {companies} {receivableTypes} fundId={fund.id} allowFundSelection
                         canManageFunds={canManageFunds}
                         onclose={() => selected = undefined}
                         onsaved={(saved: Receivable) => { receivables = [...receivables, saved]; selected = undefined; notice = 'Recebível comprado com sucesso.'; }}/>
    {/if}
    {#if buyOpen}
        <div class="modal-backdrop" role="presentation">
            <div class="modal wide" role="dialog" aria-modal="true" aria-labelledby="buy-title">
                <div class="modal-heading">
                    <div><p class="eyebrow">AQUISIÇÃO DE ATIVO</p><h2 id="buy-title">Comprar recebível</h2></div>
                    <button class="close" onclick={() => buyOpen = false} aria-label="Fechar">×</button>
                </div>
                <div class="modal-actions modal-top-action">
                    <p class="muted">Escolha um recebível disponível para este fundo.</p>
                    {#if canCreateReceivables}<button class="button secondary" onclick={() => { buyOpen = false; selected = null; }}>Cadastrar novo recebível</button>{/if}
                </div>
                {#if availableReceivables.length}
                    <div class="table-wrap"><table>
                        <thead><tr><th>Identificação</th><th>Vencimento</th><th>Valor de face</th><th></th></tr></thead>
                        <tbody>{#each availableReceivables as item}
                            <tr>
                                <td class="mono">REC-{String(item.id).padStart(4, '0')}</td>
                                <td>{date(item.dataVencimento)}</td>
                                <td>{currency(item.valorFace)}</td>
                                <td><a class="button secondary" href={`/recebiveis/${item.id}`}>Ver</a></td>
                            </tr>
                        {/each}</tbody>
                    </table></div>
                {:else}
                    <div class="empty">{canCreateReceivables ? 'Nenhum recebível disponível. Cadastre um novo recebível para continuar.' : 'Nenhum recebível disponível para compra.'}</div>
                {/if}
                <div class="modal-actions">
                    <button type="button" class="button secondary" onclick={() => buyOpen = false}>Cancelar</button>
                </div>
            </div>
        </div>
    {/if}
{:else}
    <div class="empty">Fundo não encontrado.</div>
{/if}
