import type {UserAccess} from './types';

export const scopes = {
    manageFunds: 'fundos:gerenciar',
    viewReceivables: 'recebiveis:consultar',
    createReceivables: 'recebiveis:criar',
    buyReceivables: 'recebiveis:comprar'
} as const;

export function hasRole(user: UserAccess | null | undefined, role: string) {
    return user?.roles?.some((value) => value.toUpperCase() === role.toUpperCase()) ?? false;
}

export function hasScope(user: UserAccess | null | undefined, scope: string) {
    return user?.scopes?.some((value) => value.toLowerCase() === scope.toLowerCase()) ?? false;
}
