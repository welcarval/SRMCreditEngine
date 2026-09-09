<script lang="ts">
    import {goto} from '$app/navigation';
    import {onMount} from 'svelte';
    import {auth, logout} from '$lib/auth.svelte';
    import Sidebar from '$lib/components/Sidebar.svelte';
    import Topbar from '$lib/components/Topbar.svelte';

    onMount(() => {
        if (!auth.user) goto('/login');
    });
</script>

{#if auth.user}
    <div class="app-shell">
        <Sidebar {logout}/>
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
