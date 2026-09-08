<script>
  import { login, auth } from '$lib/auth.svelte.js';

  async function startLogin() {
    try {
      await login();
    } catch (error) {
      auth.error = error.message;
    }
  }
</script>

<svelte:head><title>Entrar | Operator Portal</title></svelte:head>

<main class="login-page">
  <section class="login-card">
    <div class="login-brand"><span class="brand-mark">S</span><span><strong>SRM</strong><small>OPERATOR PORTAL</small></span></div>
    <p class="eyebrow">ACESSO OPERACIONAL</p>
    <h1>Bem-vindo de volta</h1>
    <p class="login-subtitle">Entre com sua conta corporativa para acessar a operação.</p>
    {#if auth.error}<div class="login-error" role="alert">{auth.error}</div>{/if}
    <button class="button primary login-button" onclick={startLogin}>Entrar com Keycloak</button>
    <p class="login-hint">A autenticação é gerenciada pelo provedor corporativo.</p>
  </section>
</main>
