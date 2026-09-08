<script>
  import { onMount } from 'svelte';
  import { api } from '$lib/api.js';
  import ReceivableModal from '$lib/components/ReceivableModal.svelte';
  let funds = $state([]); let companies = $state([]); let receivables = $state([]);
  let query = $state(''); let activeTab = $state('todos'); let selected = $state(undefined); let error = $state(''); let notice = $state('');
  const currency = (value) => new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(Number(value || 0));
  const days = (value) => value ? Math.ceil((new Date(`${value}T00:00:00`) - new Date()) / 86400000) : 0;
  const fundName = (item) => item?.fundo?.nome || funds.find((fund) => String(fund.id) === String(item.fundoId))?.nome || '—';
  const companyName = (item) => item?.empresa?.razaoSocial || companies.find((company) => String(company.id) === String(item.empresaId))?.razaoSocial || '—';
  let filtered = $derived(receivables.filter((item) => {
    const matchesTab = activeTab === 'todos' || (activeTab === 'vencendo' && days(item.dataVencimento) <= 30) || (activeTab === 'atrasados' && days(item.dataVencimento) < 0);
    return matchesTab && `${item.id} ${fundName(item)} ${companyName(item)}`.toLowerCase().includes(query.toLowerCase());
  }));
  onMount(async () => { try { [funds, receivables, companies] = await Promise.all([api.funds(), api.receivables(), api.companies()]); } catch (err) { error = err.message; } });
  async function remove(id) { if (!confirm('Excluir este recebível?')) return; try { await api.deleteReceivable(id); receivables = receivables.filter((item) => item.id !== id); notice = 'Recebível removido.'; } catch (err) { error = err.message; } }
</script>

<svelte:head><title>Recebíveis | Operator Portal</title></svelte:head>
{#if error}<div class="alert error">{error}</div>{/if}{#if notice}<div class="alert success">{notice}</div>{/if}
<div class="page-heading"><div><p class="eyebrow">ATIVOS DE CRÉDITO</p><h1>Recebíveis</h1><p class="muted">Controle, análise e acompanhamento dos direitos creditórios.</p></div><button class="button primary" onclick={() => selected = null}>＋ Novo recebível</button></div>
<div class="toolbar"><div class="search"><span>⌕</span><input bind:value={query} placeholder="Buscar por fundo, empresa ou ID" /></div><div class="tabs"><button class:active={activeTab === 'todos'} onclick={() => activeTab = 'todos'}>Todos <b>{receivables.length}</b></button><button class:active={activeTab === 'vencendo'} onclick={() => activeTab = 'vencendo'}>Vencendo</button><button class:active={activeTab === 'atrasados'} onclick={() => activeTab = 'atrasados'}>Em atraso</button></div></div>
<section class="panel"><div class="table-wrap"><table><thead><tr><th>Identificação</th><th>Fundo</th><th>Empresa cedente</th><th>Vencimento</th><th>Valor de face</th><th>Valor presente</th><th></th></tr></thead><tbody>
{#each filtered as item}<tr><td class="mono">REC-{String(item.id).padStart(4, '0')}</td><td><strong>{fundName(item)}</strong></td><td>{companyName(item)}</td><td class:date-warning={days(item.dataVencimento) <= 30}>{new Intl.DateTimeFormat('pt-BR').format(new Date(`${item.dataVencimento}T00:00:00`))}</td><td>{currency(item.valorFace)}</td><td><strong>{currency(item.valorPresente)}</strong></td><td class="actions"><button onclick={() => selected = item}>Editar</button><button class="danger-link" onclick={() => remove(item.id)}>Excluir</button></td></tr>{/each}
{#if !filtered.length}<tr><td colspan="7"><div class="empty">Nenhum recebível encontrado.</div></td></tr>{/if}
</tbody></table></div></section>
{#if selected !== undefined}<ReceivableModal item={selected} {funds} {companies} onclose={() => selected = undefined} onsaved={(saved) => { receivables = selected ? receivables.map((item) => item.id === saved.id ? saved : item) : [...receivables, saved]; selected = undefined; notice = 'Recebível salvo com sucesso.'; }} />{/if}
