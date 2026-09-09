import {accessToken, logout} from './auth.svelte';
import type {Company, Fund, FundPayload, Receivable, ReceivablePayload, ReceivableType} from './types';

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
    receivableTypes: () => request<ReceivableType[]>('/api/tipos-recebiveis')
};
