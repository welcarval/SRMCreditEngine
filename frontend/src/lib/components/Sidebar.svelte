<script lang="ts">
    import {page} from '$app/state';

    let {logout} = $props();
    const nav = [
        {path: '/dashboard', label: 'Visão geral', icon: '▦'},
        {path: '/recebiveis', label: 'Recebíveis', icon: '▤'},
        {path: '/fundos', label: 'Fundos', icon: '▱'}
    ];
</script>

<aside class="sidebar">
    <div class="brand"><span class="brand-mark">S</span><span><strong>SRM</strong><small>OPERATOR PORTAL</small></span>
    </div>
    <div class="workspace"><span class="status-dot"></span><span>Ambiente operacional</span><span
            class="chevron">⌄</span></div>
    <nav>
        <p class="nav-label">NAVEGAÇÃO</p>
        {#each nav as item}
            <a class:active={page.url.pathname === item.path} href={item.path}><span
                    class="nav-icon">{item.icon}</span>{item.label}</a>
        {/each}
    </nav>
    <div class="sidebar-bottom">
        <div class="help">?<span>Central de ajuda</span></div>
        <button class="user" onclick={logout} title="Sair"><span class="avatar">OM</span><span><strong>Operador</strong><small>Administrador</small></span><span
                class="chevron">↪</span></button>
    </div>
</aside>
