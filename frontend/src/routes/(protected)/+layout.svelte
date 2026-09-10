<script lang="ts">
    import {goto} from '$app/navigation';
    import {onMount} from 'svelte';
    import {auth, logout} from '$lib/auth.svelte';
    import {api} from '$lib/api';
    import type {UserAccess} from '$lib/types';
    import Sidebar from '$lib/components/Sidebar.svelte';
    import Topbar from '$lib/components/Topbar.svelte';

    let profile = $state<UserAccess | null>(null);
    onMount(async () => {
        if (!auth.user) {
            await goto('/login');
            return;
        }
        try {
            profile = await api.currentUser();
            if (location.pathname.startsWith('/usuarios') && profile.tipo.toUpperCase() !== 'ADMIN') {
                await goto('/dashboard');
            }
        } catch {
            await logout();
        }
    });
</script>

{#if auth.user}
    <div class="app-shell">
        <Sidebar {logout} isAdmin={profile?.tipo?.toUpperCase() === 'ADMIN'}/>
        <main class="main">
            <Topbar/>
            <section class="content">
                <slot/>
            </section>
        </main>
    </div>
{:else}
    <main class="login-page">
        <section class="login-card"><h1>Carregando sessão...</h1></section>
    </main>
{/if}
