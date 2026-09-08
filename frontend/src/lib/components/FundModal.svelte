<script lang="ts">
  import { api } from '$lib/api';
  import type { Fund, FundPayload } from '$lib/types';
  let { fund, onclose, onsaved }: {
    fund: Fund | null;
    onclose: () => void;
    onsaved: (saved: Fund) => void;
  } = $props();
  let form = $state<FundPayload>({ nome: fund?.nome || '', cnpj: fund?.cnpj || '', taxaBase: fund?.taxaBase ?? 0, contaId: fund?.conta?.id ?? fund?.contaId ?? null });
  let error = $state('');

  async function save() {
    try {
      const payload = { id: fund?.id, nome: form.nome, cnpj: form.cnpj, taxaBase: Number(form.taxaBase), contaId: form.contaId ? Number(form.contaId) : null };
      const saved = fund ? await api.updateFund(fund.id, payload) : await api.createFund(payload);
      onsaved(saved);
    } catch (err: unknown) { error = err instanceof Error ? err.message : 'Não foi possível salvar o fundo.'; }
  }
</script>

<div class="modal-backdrop" role="presentation" onclick={(event) => event.target === event.currentTarget && onclose()}><section class="modal" role="dialog">
  <div class="modal-heading"><div><p class="eyebrow">CADASTRO DE FUNDO</p><h2>{fund ? 'Editar fundo' : 'Novo fundo'}</h2></div><button class="close" onclick={onclose}>×</button></div>
  {#if error}<div class="login-error">{error}</div>{/if}
  <form onsubmit={(event) => { event.preventDefault(); save(); }}><label>Nome do fundo<input required bind:value={form.nome} /></label><label>CNPJ<input bind:value={form.cnpj} /></label><div class="form-grid"><label>Taxa base<input required type="number" min="0" step="0.0001" bind:value={form.taxaBase} /></label><label>ID da conta<input required type="number" min="1" bind:value={form.contaId} /></label></div><div class="modal-actions"><button type="button" class="button secondary" onclick={onclose}>Cancelar</button><button class="button primary" type="submit">Salvar fundo</button></div></form>
</section></div>
