import {afterEach, describe, expect, it, vi} from 'vitest';

const accessToken = vi.fn(() => 'test-token');
const logout = vi.fn(async () => undefined);

vi.mock('./auth.svelte', () => ({accessToken, logout}));

const {api, request} = await import('./api');

const jsonResponse = (body: unknown, status = 200) =>
    new Response(JSON.stringify(body), {status, headers: {'Content-Type': 'application/json'}});

describe('request', () => {
    afterEach(() => {
        vi.restoreAllMocks();
        accessToken.mockReturnValue('test-token');
        logout.mockClear();
    });

    it('sends JSON and bearer headers and returns the response body', async () => {
        const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse({ok: true}));

        await expect(request<{ok: boolean}>('/api/test', {method: 'POST'})).resolves.toEqual({ok: true});

        expect(fetchMock).toHaveBeenCalledWith('/api/test', expect.objectContaining({
            method: 'POST',
            headers: expect.objectContaining({
                'Content-Type': 'application/json',
                Authorization: 'Bearer test-token'
            })
        }));
    });

    it('does not send authorization when there is no token', async () => {
        accessToken.mockReturnValue('');
        vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse([]));

        await request('/api/test');

        expect(fetch).toHaveBeenCalledWith('/api/test', expect.objectContaining({
            headers: {'Content-Type': 'application/json'}
        }));
    });

    it('logs out and reports an expired session on 401', async () => {
        vi.spyOn(globalThis, 'fetch').mockResolvedValue(new Response('', {status: 401}));

        await expect(request('/api/test')).rejects.toThrow('Sua sessão expirou.');
        expect(logout).toHaveBeenCalledOnce();
    });

    it('uses the API error body for failed requests', async () => {
        vi.spyOn(globalThis, 'fetch').mockResolvedValue(new Response('Falha de negócio', {status: 422}));

        await expect(request('/api/test')).rejects.toThrow('Falha de negócio');
    });

    it('returns null for a successful empty response', async () => {
        vi.spyOn(globalThis, 'fetch').mockResolvedValue(new Response(null, {status: 204}));

        await expect(request('/api/test')).resolves.toBeNull();
    });
});

describe('api methods', () => {
    afterEach(() => vi.restoreAllMocks());

    it.each([
        ['funds', '/api/fundos', 'GET'],
        ['receivables', '/api/recebiveis', 'GET'],
        ['companies', '/api/empresas', 'GET'],
        ['receivableTypes', '/api/tipos-recebiveis', 'GET'],
        ['currentUser', '/api/usuarios/me', 'GET'],
        ['users', '/api/usuarios', 'GET']
    ])('%s requests %s', async (name, path) => {
        const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse([]));

        const apiMethod = api[name as keyof typeof api] as () => Promise<unknown>;
        await apiMethod();

        expect(fetchMock).toHaveBeenCalledWith(path, expect.objectContaining({headers: expect.any(Object)}));
    });

    it('calls fund and receivable mutations with the expected endpoints', async () => {
        vi.spyOn(globalThis, 'fetch').mockImplementation(async () => jsonResponse({id: 1}));

        await api.createFund({nome: 'Fundo', cnpj: '1', taxaBase: 0.1, contaId: null});
        await api.updateFund(1, {nome: 'Fundo', cnpj: '1', taxaBase: 0.1, contaId: null});
        await api.deleteFund(1);
        await api.createReceivable({valorFace: 1, valorPresente: 1, dataVencimento: '2026-01-01', fundoId: 1, tipoId: 1, empresaId: 1, prazo: 1, spread: 0, taxaBase: 0});
        await api.updateReceivable(1, {valorFace: 1, valorPresente: 1, dataVencimento: '2026-01-01', fundoId: 1, tipoId: 1, empresaId: 1, prazo: 1, spread: 0, taxaBase: 0});
        await api.deleteReceivable(1);

        expect(fetch).toHaveBeenCalledTimes(6);
        expect(fetch).toHaveBeenNthCalledWith(1, '/api/fundos', expect.objectContaining({method: 'POST'}));
        expect(fetch).toHaveBeenNthCalledWith(2, '/api/fundos/1', expect.objectContaining({method: 'PUT'}));
        expect(fetch).toHaveBeenNthCalledWith(3, '/api/fundos/1', expect.objectContaining({method: 'DELETE'}));
        expect(fetch).toHaveBeenNthCalledWith(4, '/api/recebiveis', expect.objectContaining({method: 'POST'}));
        expect(fetch).toHaveBeenNthCalledWith(5, '/api/recebiveis/1', expect.objectContaining({method: 'PUT'}));
        expect(fetch).toHaveBeenNthCalledWith(6, '/api/recebiveis/1', expect.objectContaining({method: 'DELETE'}));
    });

    it('associates receivables and users with funds', async () => {
        vi.spyOn(globalThis, 'fetch').mockImplementation(async () => new Response(null, {status: 204}));

        await api.addReceivableToFund(2, 3);
        await api.grantFundAccess(2, 4);
        await api.revokeFundAccess(2, 4);

        expect(fetch).toHaveBeenNthCalledWith(1, '/api/fundos/2/recebiveis/3', expect.objectContaining({method: 'POST'}));
        expect(fetch).toHaveBeenNthCalledWith(2, '/api/fundos/2/usuarios/4', expect.objectContaining({method: 'PUT'}));
        expect(fetch).toHaveBeenNthCalledWith(3, '/api/fundos/2/usuarios/4', expect.objectContaining({method: 'DELETE'}));
    });
});
