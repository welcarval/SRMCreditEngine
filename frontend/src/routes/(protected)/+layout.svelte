<script lang="ts">
    import {goto} from '$app/navigation';
    import {onMount} from 'svelte';
    import {auth, logout} from '$lib/auth.svelte';
    import {api} from '$lib/api';
    import {hasRole} from '$lib/permissions';
    import type {UserAccess} from '$lib/types';
    import Sidebar from '$lib/components/Sidebar.svelte';
    import Topbar from '$lib/components/Topbar.svelte';

    let { children } = $props();

    let profile = $state<UserAccess | null>(null);
    onMount(async () => {
        if (!auth.user) {
            await goto('/login');
            return;
        }
        try {
            profile = await api.currentUser();
            if ((location.pathname.startsWith('/usuarios') || location.pathname.startsWith('/permissoes'))
                && !hasRole(profile, 'ADMIN')) {
                await goto('/dashboard');
            }
        } catch (error) {
            // `request` encerra a sessão somente quando a API confirma um 401.
            // Não transforme erros de autorização, disponibilidade ou servidor em logout.
            console.error('Não foi possível carregar o perfil do usuário:', error);
        }
    });
</script>

{#if auth.user}
    <div class="app-shell">
        <Sidebar {logout} canManagePermissions={hasRole(profile, 'ADMIN')}/>
        <main class="main">
            <Topbar/>
            <section class="content">
                {@render children()}
            </section>
        </main>
    </div>
{:else}
    <main class="login-page">
        <section class="login-card"><h1>Carregando sessão...</h1></section>
    </main>
{/if}
