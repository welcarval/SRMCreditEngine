<script lang="ts">
    import {onMount} from 'svelte';
    import {page} from '$app/state';
    import {api} from '$lib/api';
    import {hasRole} from '$lib/permissions';
    import type {Fund, UserAccess} from '$lib/types';

    let user = $state<UserAccess | null>(null);
    let funds = $state<Fund[]>([]);
    let error = $state('');
    let notice = $state('');
    let associationOpen = $state(false);
    let selectedFundIds = $state<number[]>([]);
    let assignedFunds = $derived(funds.filter((fund) => user?.fundoIds.includes(fund.id)));
    let availableFunds = $derived(funds.filter((fund) => !user?.fundoIds.includes(fund.id)));

    onMount(async () => {
        try {
            const [users, loadedFunds] = await Promise.all([api.users(), api.funds()]);
            user = users.find((item) => item.id === Number(page.params.id) && !hasRole(item, 'ADMIN')) ?? null;
            funds = loadedFunds;
            if (!user) error = 'Usuário não encontrado.';
        } catch (err: unknown) {
            error = err instanceof Error ? err.message : 'Não foi possível carregar as permissões.';
        }
    });

    function openAssociation() {
        selectedFundIds = [];
        associationOpen = true;
    }

    async function associateFunds() {
        if (!user) return;
        try {
            await Promise.all(selectedFundIds.map((fundId) => api.grantFundAccess(fundId, user!.id)));
            user = {...user, fundoIds: [...user.fundoIds, ...selectedFundIds]};
            associationOpen = false;
            notice = selectedFundIds.length ? 'Fundos associados com sucesso.' : 'Nenhum fundo foi selecionado.';
        } catch (err: unknown) {
            error = err instanceof Error ? err.message : 'Não foi possível atualizar a permissão.';
        }
    }
</script>

<svelte:head><title>{user ? `${user.nome} | Permissões` : 'Permissões'} | Operator Portal</title></svelte:head>
<div class="page-heading">
    <div>
        <a href="/usuarios">← Voltar para permissões</a>
        <p class="eyebrow">PERMISSÕES</p>
        <h1>{user?.nome ?? 'Usuário'}</h1>
        <p class="muted">{user?.email ?? 'Carregando usuário...'}</p>
    </div>
</div>
{#if error}<div class="alert error">{error}</div>{/if}
{#if notice}<div class="alert success">{notice}</div>{/if}
{#if user}
    <section class="panel">
        <div class="page-heading">
            <div><h2>Fundos com acesso</h2><p class="muted">Fundos que este operador pode gerenciar.</p></div>
            <button class="button primary" onclick={openAssociation}>Associar fundo</button>
        </div>
        <div class="table-wrap">
            <table>
                <thead><tr><th>Fundo</th><th>CNPJ</th><th>Acesso</th></tr></thead>
                <tbody>
                {#each assignedFunds as fund}
                    <tr>
                        <td><strong>{fund.nome}</strong></td>
                        <td class="mono">{fund.cnpj || '—'}</td>
                        <td>Ativo</td>
                    </tr>
                {/each}
                {#if !assignedFunds.length}
                    <tr><td colspan="3"><div class="empty">Nenhum fundo associado.</div></td></tr>
                {/if}
                </tbody>
            </table>
        </div>
    </section>
{/if}
{#if associationOpen}
    <div class="modal-backdrop" role="presentation">
        <div class="modal" role="dialog" aria-modal="true" aria-labelledby="associate-fund-title">
            <div class="modal-heading">
                <div><p class="eyebrow">PERMISSÕES</p><h2 id="associate-fund-title">Associar fundo</h2></div>
                <button class="close" onclick={() => associationOpen = false} aria-label="Fechar">×</button>
            </div>
            <p class="muted">Selecione um ou mais fundos para liberar o acesso de {user?.nome}.</p>
            {#if availableFunds.length}
                <div class="fund-selection">
                    {#each availableFunds as fund}
                        <label class="fund-option">
                            <input type="checkbox" bind:group={selectedFundIds} value={fund.id}/>
                            <span><strong>{fund.nome}</strong><small>{fund.cnpj || 'CNPJ não informado'}</small></span>
                        </label>
                    {/each}
                </div>
            {:else}
                <div class="empty">Este usuário já possui acesso a todos os fundos.</div>
            {/if}
            <div class="modal-actions">
                <button type="button" class="button secondary" onclick={() => associationOpen = false}>Cancelar</button>
                <button type="button" class="button primary" onclick={associateFunds} disabled={!selectedFundIds.length}>
                    Confirmar
                </button>
            </div>
        </div>
    </div>
{/if}
