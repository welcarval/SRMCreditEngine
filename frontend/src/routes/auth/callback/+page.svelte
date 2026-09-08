<script>
  import { onMount } from 'svelte';
  import { completeLogin, auth } from '$lib/auth.svelte.js';

  onMount(async () => {
    try {
      await completeLogin();
    } catch (error) {
      auth.error = error.message;
    }
  });
</script>

<main class="login-page"><section class="login-card"><p class="eyebrow">AUTENTICAÇÃO</p><h1>Concluindo acesso...</h1>{#if auth.error}<div class="login-error">{auth.error}</div><a class="button secondary" href="/login">Voltar para o login</a>{/if}</section></main>
