<script>
  import '../styles.css';
  import { browser } from '$app/environment';
  import { goto } from '$app/navigation';
  import { onMount } from 'svelte';
  import { auth, initializeAuth } from '$lib/auth.svelte.js';

  onMount(async () => {
    await initializeAuth();
    if (!auth.user && window.location.pathname !== '/login' && !window.location.pathname.startsWith('/auth/')) {
      goto('/login');
    }
  });
</script>

<slot />
