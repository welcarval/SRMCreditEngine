<script lang="ts">
    import {onMount} from 'svelte';
    import {api} from '$lib/api';
    import {hasRole} from '$lib/permissions';
    import type {PermissionScope, UserAccess} from '$lib/types';

    let users = $state<UserAccess[]>([]);
    let availableScopes = $state<PermissionScope[]>([]);
    let selectedUser = $state<UserAccess | null>(null);
    let selectedScopeIds = $state<number[]>([]);
    let error = $state('');
    let notice = $state('');
    let saving = $state(false);

    onMount(async () => {
        try {
            const [loadedUsers, loadedScopes] = await Promise.all([api.users(), api.scopes()]);
            users = loadedUsers.filter((user) => !hasRole(user, 'ADMIN'));
            availableScopes = loadedScopes;
        } catch (err: unknown) {
            error = err instanceof Error ? err.message : 'Não foi possível carregar as permissões.';
        }
    });

    function selectUser(user: UserAccess) {
        selectedUser = user;
        selectedScopeIds = availableScopes
            .filter((scope) => (user.directScopes ?? []).includes(scope.codigo))
            .map((scope) => scope.id);
        notice = '';
        error = '';
    }

    async function save() {
        if (!selectedUser) return;
        saving = true;
        error = '';
        try {
            const updatedUser = await api.updateUserScopes(selectedUser.id, selectedScopeIds);
            users = users.map((user) => user.id === updatedUser.id ? updatedUser : user);
            selectedUser = updatedUser;
            notice = 'Permissões atualizadas com sucesso.';
        } catch (err: unknown) {
            error = err instanceof Error ? err.message : 'Não foi possível atualizar as permissões.';
        } finally {
            saving = false;
        }
    }
</script>

<svelte:head><title>Permissões de acesso | Operator Portal</title></svelte:head>

<div class="page-heading">
    <div>
        <p class="eyebrow">ADMINISTRAÇÃO</p>
        <h1>Permissões de acesso</h1>
        <p class="muted">Atribua permissões adicionais a operadores. Administradores não podem ser alterados nesta tela.</p>
    </div>
</div>

{#if error}<div class="alert error">{error}</div>{/if}
{#if notice}<div class="alert success">{notice}</div>{/if}

<div class="permissions-layout">
    <section class="panel">
        <div class="panel-heading"><div><h2>Operadores</h2><p>Selecione quem terá as permissões configuradas.</p></div></div>
        <div class="user-selection">
            {#each users as user}
                <button class:active={selectedUser?.id === user.id} onclick={() => selectUser(user)}>
                    <strong>{user.nome}</strong><small>{user.email}</small>
                </button>
            {/each}
            {#if !users.length}<div class="empty">Nenhum operador encontrado.</div>{/if}
        </div>
    </section>

    <section class="panel">
        <div class="panel-heading">
            <div><h2>{selectedUser ? `Permissões de ${selectedUser.nome}` : 'Selecione um operador'}</h2>
                <p>As permissões marcadas são adicionais às permissões da role do usuário.</p></div>
        </div>
        {#if selectedUser}
            <div class="scope-selection">
                {#each availableScopes as scope}
                    <label class="scope-option">
                        <input type="checkbox" bind:group={selectedScopeIds} value={scope.id}/>
                        <span><strong>{scope.descricao}</strong><small>{scope.codigo}</small></span>
                    </label>
                {/each}
            </div>
            <div class="permission-actions">
                <button class="button primary" onclick={save} disabled={saving}>{saving ? 'Salvando...' : 'Salvar permissões'}</button>
            </div>
        {:else}
            <div class="empty">Escolha um operador à esquerda para configurar suas permissões.</div>
        {/if}
    </section>
</div>
