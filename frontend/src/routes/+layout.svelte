<script lang="ts">
    import '../styles.css';
    import { browser } from '$app/environment';
    import { goto } from '$app/navigation';
    import { page } from '$app/state';
    import { onMount } from 'svelte';
    import { auth, initializeAuth } from '$lib/auth.svelte';

    let { children } = $props()

    onMount(async () => {
        await initializeAuth();
    });

    $effect(() => {
        if (!browser || auth.loading) return;

        const currentPath = page.url.pathname;
        const isPublicRoute = currentPath === '/login' || currentPath.startsWith('/auth/');

        if (!auth.user && !isPublicRoute) {
            goto('/login');
        }
    });
</script>

{#if auth.loading}
    <div class="flex h-screen items-center justify-center">
        <p>Carregando sessão...</p>
    </div>
{:else}
    {@render children()}
{/if}

