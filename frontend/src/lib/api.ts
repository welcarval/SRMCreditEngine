import {accessToken, logout} from './auth.svelte';
import type {
    Company,
    Currency,
    Fund,
    FundPayload,
    Receivable,
    ReceivablePayload,
    ReceivableType,
    StatementFilters,
    StatementTransaction,
    UserAccess,
    PermissionScope
} from './types';

const API_BASE = import.meta.env.VITE_API_URL || '';

export async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
    const token = accessToken();
    const response = await fetch(API_BASE + path, {
        ...options,
        headers: {
            'Content-Type': 'application/json',
            ...(token ? {Authorization: 'Bearer ' + token} : {}),
            ...(options.headers || {})
        }
    });
    if (response.status === 401) {
        await logout();
        throw new Error('Sua sessão expirou.');
    }
    if (!response.ok) {
        const detail = await response.text();
        throw new Error(detail || `A API retornou ${response.status}.`);
    }
    if (response.status === 204) return null as T;
    return response.json();
}

function statementQuery(filters: StatementFilters, format = 'json') {
    const params = new URLSearchParams({formato: format});
    for (const [key, value] of Object.entries(filters)) {
        if (value !== undefined && value !== null && value !== '') params.set(key, String(value));
    }
    return `/api/extratos?${params.toString()}`;
}

async function requestBlob(path: string): Promise<Blob> {
    const token = accessToken();
    const response = await fetch(API_BASE + path, {
        headers: {
            ...(token ? {Authorization: 'Bearer ' + token} : {})
        }
    });
    if (response.status === 401) {
        await logout();
        throw new Error('Sua sessão expirou.');
    }
    if (!response.ok) {
        const detail = await response.text();
        throw new Error(detail || `A API retornou ${response.status}.`);
    }
    return response.blob();
}

export const api = {
    funds: () => request<Fund[]>('/api/fundos'),
    createFund: (payload: FundPayload) => request<Fund>('/api/fundos', {method: 'POST', body: JSON.stringify(payload)}),
    updateFund: (id: number, payload: FundPayload) => request<Fund>('/api/fundos/' + id, {
        method: 'PUT',
        body: JSON.stringify(payload)
    }),
    deleteFund: (id: number) => request<void>('/api/fundos/' + id, {method: 'DELETE'}),
    receivables: () => request<Receivable[]>('/api/recebiveis'),
    createReceivable: (payload: ReceivablePayload) => request<Receivable>('/api/recebiveis', {
        method: 'POST',
        body: JSON.stringify(payload)
    }),
    updateReceivable: (id: number, payload: ReceivablePayload) => request<Receivable>('/api/recebiveis/' + id, {
        method: 'PUT',
        body: JSON.stringify(payload)
    }),
    deleteReceivable: (id: number) => request<void>('/api/recebiveis/' + id, {method: 'DELETE'}),
    companies: () => request<Company[]>('/api/empresas'),
    currencies: () => request<Currency[]>('/api/moedas'),
    receivableTypes: () => request<ReceivableType[]>('/api/tipos-recebiveis'),
    currentUser: () => request<UserAccess>('/api/usuarios/me'),
    users: () => request<UserAccess[]>('/api/usuarios'),
    scopes: () => request<PermissionScope[]>('/api/scopes'),
    updateUserScopes: (userId: number, scopeIds: number[]) =>
        request<UserAccess>(`/api/usuarios/${userId}/scopes`, {method: 'PUT', body: JSON.stringify({scopeIds})}),
    statement: (filters: StatementFilters) => request<StatementTransaction[]>(statementQuery(filters)),
    downloadStatement: (filters: StatementFilters, format: 'csv' | 'parquet') =>
        requestBlob(statementQuery(filters, format)),
    addReceivableToFund: (fundId: number, receivableId: number) =>
        request<Receivable>(`/api/fundos/${fundId}/recebiveis/${receivableId}`, {method: 'POST'}),
    grantFundAccess: (fundId: number, userId: number) =>
        request<void>(`/api/fundos/${fundId}/usuarios/${userId}`, {method: 'PUT'}),
    revokeFundAccess: (fundId: number, userId: number) =>
        request<void>(`/api/fundos/${fundId}/usuarios/${userId}`, {method: 'DELETE'})
};
