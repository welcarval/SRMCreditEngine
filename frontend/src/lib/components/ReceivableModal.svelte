<script lang="ts">
    import {api} from '$lib/api';
    import type {Company, Fund, Receivable, ReceivablePayload, ReceivableType} from '$lib/types';

    let {
        item,
        funds,
        companies,
        receivableTypes,
        fundId = 0,
        canManageFunds = false,
        allowFundSelection = false,
        onclose,
        onsaved
    }: {
        item: Receivable | null;
        funds: Fund[];
        companies: Company[];
        receivableTypes: ReceivableType[];
        fundId?: number;
        canManageFunds?: boolean;
        allowFundSelection?: boolean;
        onclose: () => void;
        onsaved: (saved: Receivable) => void;
    } = $props();

    let form = $state<ReceivablePayload>({
        valorFace: item?.valorFace ?? 0,
        dataVencimento: item?.dataVencimento || '',
        fundoId: item?.fundo?.id ?? item?.fundoId ?? (fundId > 0 ? fundId : null),
        tipoId: item?.tipo?.id ?? item?.tipoId ?? 0,
        empresaId: item?.empresa?.id ?? item?.empresaId ?? 0,
        taxaBase: item?.taxaBase != null ? item.taxaBase * 100 : 0
    });
    let error = $state('');

    async function save() {
        try {
            const taxaPercentual = Number(form.taxaBase);
            if (!Number.isFinite(taxaPercentual) || taxaPercentual < 0 || taxaPercentual > 100) {
                error = 'A taxa base deve estar entre 0% e 100%.';
                return;
            }
            const payload = {
                id: item?.id,
                valorFace: Number(form.valorFace),
                dataVencimento: form.dataVencimento,
                fundoId: form.fundoId ? Number(form.fundoId) : null,
                tipoId: Number(form.tipoId),
                empresaId: Number(form.empresaId),
                taxaBase: taxaPercentual / 100
            };
            const saved = item ? await api.updateReceivable(item.id, payload) : await api.createReceivable(payload);
            onsaved(saved);
        } catch (err: unknown) {
            error = err instanceof Error ? err.message : 'Não foi possível salvar o recebível.';
        }
    }
</script>

<div class="modal-backdrop" role="presentation" onclick={(event) => event.target === event.currentTarget && onclose()}>
    <section class="modal wide" role="dialog">
        <div class="modal-heading">
            <div>
                <p class="eyebrow">ATIVO DE CRÉDITO</p>
                <h2>{item ? 'Editar recebível' : 'Novo recebível'}</h2>
            </div>
            <button class="close" onclick={onclose}>×</button>
        </div>
        {#if error}
            <div class="login-error">{error}</div>
        {/if}
        <form onsubmit={(event) => { event.preventDefault(); save(); }}>
            <div class="form-grid">
                <label>Valor de face (R$)<input required type="number" min="0" step="0.01"
                                                bind:value={form.valorFace}/>
                </label>
                <label>
                    Data de vencimento
                    <input required type="date" bind:value={form.dataVencimento}/>
                </label>
                {#if allowFundSelection}
                    <label>
                        Fundo
                        <select bind:value={form.fundoId} disabled={fundId > 0 || !canManageFunds}>
                            <option value={null}>Selecione</option>
                            {#if canManageFunds}
                                {#each funds as fund}
                                    <option value={fund.id}>{fund.nome}</option>
                                {/each}
                            {/if}
                        </select>
                    </label>
                {/if}
                <label>
                    Empresa cedente
                    <select required bind:value={form.empresaId}>
                        <option value="">Selecione</option>
                        {#each companies as company}
                            <option value={company.id}>{company.razaoSocial}</option>
                        {/each}
                    </select>
                </label>
                <label>
                    Tipo de recebível
                    <select required bind:value={form.tipoId}>
                        <option value="">Selecione</option>
                        {#each receivableTypes as type}
                            <option value={type.id}>{type.nome}</option>
                        {/each}
                    </select>
                </label>
                <label>
                    Taxa base
                    <input required type="number" min="0" max="100" step="0.01" bind:value={form.taxaBase}/>
                </label>
            </div>
            <div class="modal-actions">
                <button type="button" class="button secondary" onclick={onclose}>Cancelar</button>
                <button class="button primary" type="submit">Salvar recebível</button>
            </div>
        </form>
    </section>
</div>
