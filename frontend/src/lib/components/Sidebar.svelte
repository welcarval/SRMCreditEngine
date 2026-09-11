<script lang="ts">
    import {page} from '$app/state';

    let {logout, canManagePermissions = false} = $props<{
        logout: () => Promise<void>;
        canManagePermissions?: boolean;
    }>();
    const nav = [
        {path: '/dashboard', label: 'Visão geral', icon: '▦'},
        {path: '/recebiveis', label: 'Recebíveis', icon: '▤'},
        {path: '/fundos', label: 'Fundos', icon: '▱'}
    ];
    let authorizedNav = $derived(canManagePermissions
        ? [...nav,
            {path: '/extratos', label: 'Extratos', icon: '▥'},
            {path: '/usuarios', label: 'Fundos por usuário', icon: '♙'},
            {path: '/permissoes', label: 'Permissões', icon: '⚿'}]
        : nav);
</script>

<aside class="sidebar">
    <div class="brand"><span class="brand-mark">S</span><span><strong>SRM</strong><small>OPERATOR PORTAL</small></span>
    </div>
    <nav>
        <p class="nav-label">NAVEGAÇÃO</p>
        {#each authorizedNav as item}
            <a class:active={page.url.pathname === item.path || page.url.pathname.startsWith(`${item.path}/`)} href={item.path}><span
                    class="nav-icon">{item.icon}</span>{item.label}</a>
        {/each}
    </nav>
    <div class="sidebar-bottom">
        <div class="help">?<span>Central de ajuda</span></div>
        <button class="user" onclick={logout} title="Sair"><span class="avatar">OM</span><span><strong>Operador</strong><small>Administrador</small></span><span
                class="chevron">↪</span></button>
    </div>
</aside>
