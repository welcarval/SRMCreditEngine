<script lang="ts">
    import {onMount} from 'svelte';
    import {api} from '$lib/api';
    import {hasRole} from '$lib/permissions';
    import type {UserAccess} from '$lib/types';

    let users = $state<UserAccess[]>([]);
    let error = $state('');

    onMount(async () => {
        try {
            users = (await api.users()).filter((user) => !hasRole(user, 'ADMIN'));
        } catch (err: unknown) {
            error = err instanceof Error ? err.message : 'Não foi possível carregar as permissões.';
        }
    });
</script>

<svelte:head><title>Permissões | Operator Portal</title></svelte:head>
<div class="page-heading">
    <div>
        <p class="eyebrow">ADMINISTRAÇÃO</p>
        <h1>Permissões</h1>
        <p class="muted">Selecione um usuário para associar os fundos que ele pode acessar.</p>
    </div>
</div>
{#if error}<div class="alert error">{error}</div>{/if}
<section class="panel">
    <div class="table-wrap">
        <table>
            <thead><tr><th>Usuário</th><th>E-mail</th><th>Roles</th><th>Fundos associados</th><th></th></tr></thead>
            <tbody>
            {#each users as user}
                <tr>
                    <td><strong>{user.nome}</strong></td>
                    <td>{user.email}</td>
                    <td>{user.roles.join(', ') || '—'}</td>
                    <td>{user.fundoIds.length}</td>
                    <td class="actions"><a href={`/usuarios/${user.id}`}>Gerenciar fundos</a></td>
                </tr>
            {/each}
            {#if !users.length}
                <tr><td colspan="5"><div class="empty">Nenhum usuário não administrador encontrado.</div></td></tr>
            {/if}
            </tbody>
        </table>
    </div>
</section>
