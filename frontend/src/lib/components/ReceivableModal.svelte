<script>
  import { api } from '$lib/api.js';
  let { item = null, funds = [], companies = [], onclose = () => {}, onsaved = () => {} } = $props();
  let form = $state({
    valorFace: item?.valorFace ?? '', dataVencimento: item?.dataVencimento || '',
    fundoId: item?.fundo?.id ?? item?.fundoId ?? '', tipoId: item?.tipo?.id ?? item?.tipoId ?? '',
    empresaId: item?.empresa?.id ?? item?.empresaId ?? '', prazo: item?.prazo ?? '',
    spread: item?.spread ?? item?.tipo?.spread ?? '', taxaBase: item?.taxaBase ?? item?.fundo?.taxaBase ?? ''
  });
  let error = $state('');
  let days = $derived(form.dataVencimento ? Math.ceil((new Date(`${form.dataVencimento}T00:00:00`) - new Date()) / 86400000) : 0);
  let presentValue = $derived(form.valorFace ? Number(form.valorFace) / Math.pow(1 + Number(form.taxaBase || 0) + Number(form.spread || 0), Math.max(days / 365, 0)) : 0);

  async function save() {
    try {
      const payload = { id: item?.id, valorFace: Number(form.valorFace), valorPresente: presentValue, dataVencimento: form.dataVencimento, fundoId: Number(form.fundoId), tipoId: Number(form.tipoId), empresaId: Number(form.empresaId), prazo: Number(form.prazo || days / 365), spread: Number(form.spread || 0), taxaBase: Number(form.taxaBase || 0) };
      const saved = item ? await api.updateReceivable(item.id, payload) : await api.createReceivable(payload);
      onsaved(saved);
    } catch (err) { error = err.message; }
  }
</script>

<div class="modal-backdrop" role="presentation" onclick={(event) => event.target === event.currentTarget && onclose()}><section class="modal wide" role="dialog">
  <div class="modal-heading"><div><p class="eyebrow">ATIVO DE CRÉDITO</p><h2>{item ? 'Editar recebível' : 'Novo recebível'}</h2></div><button class="close" onclick={onclose}>×</button></div>
  {#if error}<div class="login-error">{error}</div>{/if}
  <form onsubmit={(event) => { event.preventDefault(); save(); }}><div class="form-grid"><label>Valor de face (R$)<input required type="number" min="0" step="0.01" bind:value={form.valorFace} /></label><label>Data de vencimento<input required type="date" bind:value={form.dataVencimento} /></label><label>Fundo<select required bind:value={form.fundoId}><option value="">Selecione</option>{#each funds as fund}<option value={fund.id}>{fund.nome}</option>{/each}</select></label><label>Empresa cedente<select required bind:value={form.empresaId}><option value="">Selecione</option>{#each companies as company}<option value={company.id}>{company.razaoSocial}</option>{/each}</select></label><label>ID do tipo<input required type="number" min="1" bind:value={form.tipoId} /></label><label>Spread<input type="number" min="0" step="0.0001" bind:value={form.spread} /></label><label>Taxa base<input type="number" min="0" step="0.0001" bind:value={form.taxaBase} /></label><label>Prazo em anos<input type="number" min="0" step="0.0001" bind:value={form.prazo} /></label></div><div class="calculation"><span><small>VALOR PRESENTE</small><strong>{new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(presentValue)}</strong></span><span><small>DIAS ATÉ O VENCIMENTO</small><strong>{Math.max(days, 0)} dias</strong></span></div><div class="modal-actions"><button type="button" class="button secondary" onclick={onclose}>Cancelar</button><button class="button primary" type="submit">Salvar recebível</button></div></form>
</section></div>
