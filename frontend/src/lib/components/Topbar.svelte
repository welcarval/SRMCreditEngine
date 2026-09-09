<script lang="ts">
    import {page} from '$app/state';

    const labels: Record<string, string> = {
        '/dashboard': 'Visão geral',
        '/fundos': 'Fundos',
        '/recebiveis': 'Recebíveis'
    };
</script>

<header class="topbar">
    <div class="breadcrumbs"><span>Operações</span><b>/</b><strong>{labels[page.url.pathname] || 'Operações'}</strong>
    </div>
    <div class="top-actions">
        <button class="icon-button" aria-label="Notificações">♢<i></i></button>
        <div class="date-stamp">08 SET 2026</div>
    </div>
</header>
